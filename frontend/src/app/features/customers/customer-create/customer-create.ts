import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Breadcrumb, BreadcrumbItem } from '../../../shared/ui/breadcrumb/breadcrumb';
import { Stepper, StepperItem } from '../../../shared/ui/stepper/stepper';
import { Customer, CustomerAddress, CustomerContact } from '../customer.model';
import { CustomerService } from '../customer.service';
import { CustomerAddressStep } from './customer-address-step';
import { ContactStepDraft, CustomerContactStep, emptyContactDraft } from './customer-contact-step';
import { CustomerDemographicForm, CustomerDraft } from './customer-demographic-form';

/** Etiket için sonda görünen dial kodunu ("+90") çıkarır. */
function dialCode(countryCodeLabel: string): string {
  return countryCodeLabel.match(/\(([^)]+)\)/)?.[1] ?? '';
}

/** Müşteri oluşturma sihirbazı; adım durumunu ve toplanan verileri tutar. */
@Component({
  selector: 'app-customer-create',
  imports: [
    Breadcrumb,
    Stepper,
    CustomerDemographicForm,
    CustomerAddressStep,
    CustomerContactStep
  ],
  templateUrl: './customer-create.html'
})
export class CustomerCreate {
  private readonly customers = inject(CustomerService);
  private readonly router = inject(Router);

  protected readonly t = inject(I18nService).t;

  protected readonly activeStep = signal(1);

  /** Adımlarda toplanan veriler; geri/ileri gezinmede korunur. */
  protected readonly demographic = signal<CustomerDraft | null>(null);
  protected readonly addresses = signal<CustomerAddress[]>([]);
  protected readonly contact = signal<ContactStepDraft>(emptyContactDraft());

  protected readonly steps = computed<readonly StepperItem[]>(() => {
    const steps = this.t().customers.create.steps;

    return [
      { index: 1, label: steps.demographic },
      { index: 2, label: steps.address },
      { index: 3, label: steps.contact }
    ];
  });

  protected readonly breadcrumb = computed<readonly BreadcrumbItem[]>(() => [
    { label: this.t().nav.customerSearch, link: '/customers' },
    { label: this.t().nav.customerCreate }
  ]);

  protected goToStep(step: number): void {
    this.activeStep.set(step);
  }

  /** 1. adımdan gelen demografik taslağı saklayıp adres adımına geçer. */
  protected onDemographicNext(draft: CustomerDraft): void {
    this.demographic.set(draft);
    this.goToStep(2);
  }

  /** 3. adımdaki "Oluştur"; toplanan verilerden müşteriyi kurar ve detayına gider. */
  protected create(): void {
    const demographic = this.demographic();
    if (demographic === null) {
      return;
    }

    const draft = this.contact();
    const dial = dialCode(draft.countryCode);
    const contact: CustomerContact = {
      email: draft.email.trim() === '' ? null : draft.email.trim(),
      mobilePhone:
        draft.mobilePhone.trim() === '' ? null : `${dial} ${draft.mobilePhone.trim()}`.trim(),
      homePhone: draft.homePhone.trim() === '' ? null : draft.homePhone.trim(),
      fax: draft.fax.trim() === '' ? null : draft.fax.trim()
    };

    const id = Date.now();
    const firstAddressCity = this.addresses()[0]?.title.split(', ')[0] ?? '';

    const customer: Customer = {
      id,
      code: `C-${id}`,
      type: 'B2C',
      firstName: demographic.firstName,
      secondName: demographic.secondName.trim() === '' ? null : demographic.secondName,
      lastName: demographic.lastName,
      companyName: null,
      identityNumber: demographic.identityNumber,
      gender: demographic.gender,
      birthDate: demographic.birthDate,
      motherName: demographic.motherName,
      fatherName: demographic.fatherName,
      accountNumber: '',
      gsm: draft.mobilePhone.trim(),
      city: firstAddressCity,
      status: 'active',
      registeredAt: new Date().toISOString().slice(0, 10),
      contact,
      addresses: this.addresses(),
      accounts: [],
      orders: []
    };

    this.customers.create(customer);
    void this.router.navigate(['/customers', id]);
  }
}
