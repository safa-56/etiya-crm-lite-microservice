import { Component, inject, input, linkedSignal, output } from '@angular/core';
import { FormField, form } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { DigitsOnly } from '../../../shared/directives/digits-only';
import { LettersOnly } from '../../../shared/directives/letters-only';
import { Button } from '../../../shared/ui/button/button';
import { ButtonLink } from '../../../shared/ui/button/button-link';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerDraft, customerDemographicSchema, todayIsoDate } from '../customer-demographic.schema';

/** Sihirbazın 1. adımı: demografik bilgi formu. */
@Component({
  selector: 'app-customer-demographic-form',
  imports: [
    FormField,
    Button,
    ButtonLink,
    FormFieldShell,
    Icon,
    PanelHeader,
    DigitsOnly,
    LettersOnly
  ],
  host: { class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm' },
  templateUrl: './customer-demographic-form.html'
})
export class CustomerDemographicForm {
  protected readonly t = inject(I18nService).t;

  /** Doğrulanan demografik taslağı bir üst sihirbaza taşır. */
  readonly next = output<CustomerDraft>();

  /** Sihirbaz geri geldiğinde alanların dolu kalması için dışarıdan verilir. */
  readonly initial = input<CustomerDraft | null>(null);

  private readonly draft = linkedSignal<CustomerDraft | null, CustomerDraft>({
    source: this.initial,
    computation: (initial) =>
      initial ?? {
        firstName: '',
        secondName: '',
        lastName: '',
        birthDate: '',
        gender: 'male',
        fatherName: '',
        motherName: '',
        identityNumber: ''
      }
  });

  protected readonly customerForm = form(this.draft, customerDemographicSchema);

  /** Doğum tarihi seçicisinin izin verdiği en ileri gün (bugün) — ileri tarih engellenir. */
  protected readonly maxBirthDate = todayIsoDate();

  protected emitNext(): void {
    this.next.emit(this.draft());
  }
}
