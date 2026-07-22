import { Component, inject, input, linkedSignal, signal } from '@angular/core';
import { FormField, form, required, schema } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { DetailField } from '../../../shared/ui/detail-field/detail-field';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerContact } from '../customer.model';
import { CustomerService } from '../customer.service';

/** Cep telefonu alanındaki ülke kodu seçenekleri. */
const COUNTRY_CODES: readonly string[] = [
  'Türkiye (+90)',
  'Almanya (+49)',
  'İngiltere (+44)',
  'ABD (+1)',
  'Fransa (+33)'
];

interface ContactDraft {
  email: string;
  homePhone: string;
  countryCode: string;
  mobilePhone: string;
  fax: string;
}

function toDraft(contact: CustomerContact): ContactDraft {
  return {
    email: contact.email ?? '',
    homePhone: contact.homePhone ?? '',
    countryCode: COUNTRY_CODES[0],
    mobilePhone: contact.mobilePhone ?? '',
    fax: contact.fax ?? ''
  };
}

const contactDraftSchema = schema<ContactDraft>((draft) => {
  required(draft.email);
  required(draft.mobilePhone);
});

/**
 * "İletişim Kanalı" sekmesi. Varsayılan olarak salt okunur listeyi gösterir; kalem düğmesine
 * basıldığında aynı alanları düzenlenebilir bir forma çevirir.
 */
@Component({
  selector: 'app-customer-contact-panel',
  imports: [FormField, Button, DetailField, FormFieldShell, Icon, IconButton, PanelHeader],
  host: {
    role: 'tabpanel',
    id: 'panel-contact',
    'aria-labelledby': 'tab-contact',
    tabindex: '0',
    class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm'
  },
  template: `
    <app-panel-header [heading]="t().customers.detail.contact.title">
      @if (!isEditing()) {
        <app-icon-button
          inline
          icon="pencil"
          tone="edit"
          [label]="t().customers.detail.contact.edit"
          (click)="startEdit()"
        />
      }
    </app-panel-header>

    @if (isEditing()) {
      <form (submit)="save($event)">
        <div class="mt-6 grid gap-x-8 gap-y-5 sm:grid-cols-2">
          <app-form-field
            for="contact-email"
            [label]="t().customers.detail.contact.email"
            [required]="true"
          >
            <input
              id="contact-email"
              type="email"
              class="field-control"
              [formField]="contactForm.email"
              aria-required="true"
            />
          </app-form-field>

          <app-form-field for="contact-home" [label]="t().customers.detail.contact.homePhone">
            <input
              id="contact-home"
              type="tel"
              class="field-control"
              [formField]="contactForm.homePhone"
            />
          </app-form-field>

          <app-form-field
            for="contact-mobile"
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
                id="contact-mobile"
                type="tel"
                inputmode="numeric"
                class="field-control"
                [formField]="contactForm.mobilePhone"
                aria-required="true"
              />
            </div>
          </app-form-field>

          <app-form-field for="contact-fax" [label]="t().customers.detail.contact.fax">
            <input
              id="contact-fax"
              type="tel"
              class="field-control"
              [formField]="contactForm.fax"
            />
          </app-form-field>
        </div>

        <div class="mt-8 flex items-center justify-end gap-3 border-t border-slate-100 pt-5">
          <app-button type="button" variant="outline" size="lg" (click)="cancel()">
            {{ t().customers.detail.contact.cancel }}
          </app-button>

          <app-button type="submit" size="lg" [disabled]="contactForm().invalid()">
            <app-icon name="save" [stroke]="1.8" />
            {{ t().customers.detail.contact.save }}
          </app-button>
        </div>
      </form>
    } @else {
      <dl class="mt-5 grid gap-x-12 gap-y-5 sm:grid-cols-2">
        <div
          appDetailField
          [breakAll]="true"
          [label]="t().customers.detail.contact.email"
          [value]="contactState().email ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.contact.homePhone"
          [value]="contactState().homePhone ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.contact.mobilePhone"
          [value]="contactState().mobilePhone ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.contact.fax"
          [value]="contactState().fax ?? t().common.empty"
        ></div>
      </dl>
    }
  `
})
export class CustomerContactPanel {
  private readonly customers = inject(CustomerService);

  protected readonly t = inject(I18nService).t;

  readonly contact = input.required<CustomerContact>();
  readonly customerId = input.required<number>();

  protected readonly countryCodes = COUNTRY_CODES;

  /** Kaydedilen değerler bu yerel duruma yazılır; görünüm buradan okur. */
  protected readonly contactState = linkedSignal<CustomerContact, CustomerContact>({
    source: this.contact,
    computation: (contact) => contact
  });

  protected readonly isEditing = signal(false);

  private readonly draft = linkedSignal<CustomerContact, ContactDraft>({
    source: this.contactState,
    computation: (contact) => toDraft(contact)
  });

  protected readonly contactForm = form(this.draft, contactDraftSchema);

  protected startEdit(): void {
    this.draft.set(toDraft(this.contactState()));
    this.isEditing.set(true);
  }

  protected cancel(): void {
    this.draft.set(toDraft(this.contactState()));
    this.isEditing.set(false);
  }

  protected save(event: Event): void {
    event.preventDefault();
    if (this.contactForm().invalid()) {
      return;
    }

    const draft = this.draft();
    const contact: CustomerContact = {
      email: draft.email.trim() === '' ? null : draft.email.trim(),
      homePhone: draft.homePhone.trim() === '' ? null : draft.homePhone.trim(),
      mobilePhone: draft.mobilePhone.trim() === '' ? null : draft.mobilePhone.trim(),
      fax: draft.fax.trim() === '' ? null : draft.fax.trim()
    };

    this.contactState.set(contact);
    this.customers.update(this.customerId(), { contact });
    this.isEditing.set(false);
  }
}
