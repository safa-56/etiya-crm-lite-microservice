import { Component, computed, inject, signal } from '@angular/core';
import { FormField, form, maxLength, required, schema } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Gender } from '../customer.model';

/** Sihirbazın ilk adımında toplanan demografik alanlar. */
interface CustomerDraft {
  firstName: string;
  secondName: string;
  lastName: string;
  birthDate: string;
  gender: Gender;
  fatherName: string;
  motherName: string;
  identityNumber: string;
}

const customerDraftSchema = schema<CustomerDraft>((draft) => {
  required(draft.firstName);
  required(draft.lastName);
  required(draft.birthDate);
  required(draft.gender);
  required(draft.identityNumber);
  maxLength(draft.identityNumber, 11);
});

@Component({
  selector: 'app-customer-create',
  imports: [FormField, RouterLink],
  templateUrl: './customer-create.html'
})
export class CustomerCreate {
  protected readonly t = inject(I18nService).t;

  private readonly draft = signal<CustomerDraft>({
    firstName: '',
    secondName: '',
    lastName: '',
    birthDate: '',
    gender: 'male',
    fatherName: '',
    motherName: '',
    identityNumber: ''
  });

  protected readonly customerForm = form(this.draft, customerDraftSchema);

  /** Adres ve iletişim adımları tasarlandığında bu değer ilerletilecek. */
  protected readonly activeStep = signal(1);

  protected readonly steps = computed(() => {
    const steps = this.t().customers.create.steps;

    return [
      { index: 1, label: steps.demographic },
      { index: 2, label: steps.address },
      { index: 3, label: steps.contact }
    ];
  });
}
