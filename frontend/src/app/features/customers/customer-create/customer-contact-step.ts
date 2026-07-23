import { Component, inject, model, output } from '@angular/core';
import { FormField, form, required, schema } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';

/** Cep telefonu alanındaki ülke kodu seçenekleri. */
export const COUNTRY_CODES: readonly string[] = [
  'Türkiye (+90)',
  'Almanya (+49)',
  'İngiltere (+44)',
  'ABD (+1)',
  'Fransa (+33)'
];

/** Sihirbazın iletişim adımında toplanan alanlar. */
export interface ContactStepDraft {
  email: string;
  homePhone: string;
  countryCode: string;
  mobilePhone: string;
  fax: string;
}

/** Boş bir iletişim taslağı; sihirbazın başlangıç durumu için kullanılır. */
export function emptyContactDraft(): ContactStepDraft {
  return {
    email: '',
    homePhone: '',
    countryCode: COUNTRY_CODES[0],
    mobilePhone: '',
    fax: ''
  };
}

const contactStepSchema = schema<ContactStepDraft>((draft) => {
  required(draft.email);
  required(draft.mobilePhone);
});

/** Sihirbazın 3. adımı: iletişim kanalı bilgileri ve müşteriyi oluşturma. */
@Component({
  selector: 'app-customer-contact-step',
  imports: [FormField, Button, FormFieldShell, Icon, PanelHeader],
  host: { class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm' },
  template: `
    <app-panel-header [heading]="t().customers.detail.contact.title" />

    <form (submit)="submit($event)">
      <div class="mt-6 grid gap-x-8 gap-y-5 sm:grid-cols-2">
        <app-form-field
          for="new-contact-email"
          [label]="t().customers.detail.contact.email"
          [required]="true"
        >
          <input
            id="new-contact-email"
            type="email"
            class="field-control"
            [placeholder]="t().customers.detail.contact.emailPlaceholder"
            [formField]="contactForm.email"
            aria-required="true"
          />
        </app-form-field>

        <app-form-field for="new-contact-home" [label]="t().customers.detail.contact.homePhone">
          <input
            id="new-contact-home"
            type="tel"
            class="field-control"
            [formField]="contactForm.homePhone"
          />
        </app-form-field>

        <app-form-field
          for="new-contact-mobile"
          [label]="t().customers.detail.contact.mobilePhone"
          [required]="true"
        >
          <div class="flex gap-3">
            <select
              class="field-control w-40 shrink-0"
              [formField]="contactForm.countryCode"
              [attr.aria-label]="t().customers.detail.contact.countryCode"
            >
              @for (code of countryCodes; track code) {
                <option [value]="code">{{ code }}</option>
              }
            </select>
            <input
              id="new-contact-mobile"
              type="tel"
              inputmode="numeric"
              class="field-control"
              [placeholder]="t().customers.detail.contact.mobilePlaceholder"
              [formField]="contactForm.mobilePhone"
              aria-required="true"
            />
          </div>
        </app-form-field>

        <app-form-field for="new-contact-fax" [label]="t().customers.detail.contact.fax">
          <input
            id="new-contact-fax"
            type="tel"
            class="field-control"
            [formField]="contactForm.fax"
          />
        </app-form-field>
      </div>

      <div class="mt-8 flex items-center justify-between gap-3 border-t border-slate-100 pt-5">
        <app-button type="button" variant="outline" size="lg" (click)="back.emit()">
          <app-icon name="arrow-left" [stroke]="1.8" />
          {{ t().customers.create.back }}
        </app-button>

        <app-button type="submit" size="lg" [disabled]="contactForm().invalid()">
          <app-icon name="check" [stroke]="1.8" />
          {{ t().customers.create.create }}
        </app-button>
      </div>
    </form>
  `
})
export class CustomerContactStep {
  protected readonly t = inject(I18nService).t;

  /** İki yönlü bağlanır; sihirbaz adımlar arası geçişte alanları korur. */
  readonly draft = model.required<ContactStepDraft>();

  readonly back = output<void>();
  readonly create = output<void>();

  protected readonly countryCodes = COUNTRY_CODES;

  protected readonly contactForm = form(this.draft, contactStepSchema);

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.contactForm().invalid()) {
      return;
    }

    this.create.emit();
  }
}
