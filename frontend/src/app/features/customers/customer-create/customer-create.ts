import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Breadcrumb, BreadcrumbItem } from '../../../shared/ui/breadcrumb/breadcrumb';
import { Stepper, StepperItem } from '../../../shared/ui/stepper/stepper';
import { CustomerAddress } from '../customer.model';
import { CreateIndividualCustomerBody, CustomerService } from '../customer.service';
import { CustomerAddressStep } from './customer-address-step';
import { ContactStepDraft, CustomerContactStep, emptyContactDraft } from './customer-contact-step';
import { CustomerDraft } from '../customer-demographic.schema';
import { CustomerDemographicForm } from './customer-demographic-form';

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

  /** Kaydetme durumu: çift gönderimi engeller ve hata mesajını yönetir. */
  protected readonly submitting = signal(false);
  protected readonly submitError = signal(false);
  /** Backend'in 400 ile döndüğü alan bazlı doğrulama mesajları (hangi alan hatalı). */
  protected readonly submitFieldErrors = signal<readonly string[]>([]);

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

  /**
   * 3. adımdaki "Oluştur": toplanan verilerden backend isteğini kurup customer-service'e
   * (gerçek DB) yazar. Başarıda oluşan müşterinin detayına gider; hatada mesaj gösterir.
   *
   * <p>Not: backend tek adres (birincil) kabul eder; birden çok adres eklendiyse birincil
   * gönderilir. Cep telefonu backend size (11-15) kuralına takılmaması için ülke kodu
   * öneki olmadan, girilen numarayla gönderilir.
   */
  protected create(): void {
    const demographic = this.demographic();
    const primary = this.addresses().find((address) => address.isPrimary) ?? this.addresses()[0] ?? null;

    if (demographic === null || primary === null || this.submitting()) {
      return;
    }

    const draft = this.contact();
    const trimmedOrNull = (value: string): string | null =>
      value.trim() === '' ? null : value.trim();

    // Yapısal adres alanları oluşturma adımında dolar; eski/eksik kayıt için title'dan türetilir.
    const [titleCity, titleStreet, titleHouseNumber] = primary.title.split(', ');

    const body: CreateIndividualCustomerBody = {
      firstName: demographic.firstName.trim(),
      secondName: trimmedOrNull(demographic.secondName),
      lastName: demographic.lastName.trim(),
      birthDate: demographic.birthDate,
      fatherName: trimmedOrNull(demographic.fatherName),
      motherName: trimmedOrNull(demographic.motherName),
      nationalityId: demographic.identityNumber.trim(),
      genderType: demographic.gender === 'female' ? 'FEMALE' : 'MALE',
      contactInfo: {
        email: draft.email.trim(),
        homePhone: trimmedOrNull(draft.homePhone),
        mobilePhone: draft.mobilePhone.trim(),
        fax: trimmedOrNull(draft.fax)
      },
      address: {
        city: primary.city ?? titleCity ?? '',
        street: primary.street ?? titleStreet ?? '',
        houseNumber: primary.houseNumber ?? titleHouseNumber ?? '',
        addressDescription: primary.detail,
        isPrimary: true
      }
    };

    this.submitting.set(true);
    this.submitError.set(false);
    this.submitFieldErrors.set([]);

    this.customers.createIndividual(body).subscribe({
      next: (id) => {
        this.submitting.set(false);
        void this.router.navigate(['/customers', id]);
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        const fieldErrors = this.extractFieldErrors(error);
        if (fieldErrors.length > 0) {
          this.submitFieldErrors.set(fieldErrors);
        } else {
          this.submitError.set(true);
        }
      }
    });
  }

  /**
   * Backend'in 400 doğrulama yanıtından ({@code ProblemDetail.validationErrors})
   * alan bazlı mesajları çıkarır. Mesajlar backend'de dile göre çözülmüştür.
   */
  private extractFieldErrors(error: unknown): readonly string[] {
    if (error instanceof HttpErrorResponse && error.error && typeof error.error === 'object') {
      const validationErrors = (error.error as { validationErrors?: Record<string, string> })
        .validationErrors;
      if (validationErrors) {
        return Object.values(validationErrors);
      }
    }
    return [];
  }
}
