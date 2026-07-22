import { Component, computed, inject, input, linkedSignal, output } from '@angular/core';
import { FormField, form, required, schema } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerAccount, CustomerAddress } from '../customer.model';
import { CustomerAddressCard } from './customer-address-card';

/** Formun ürettiği sonuç; hesap panelinde listeye işlenir. */
export interface AccountFormResult {
  name: string;
  addressDescription: string;
  addressId: string | null;
}

interface AccountDraft {
  name: string;
  addressDescription: string;
}

const accountDraftSchema = schema<AccountDraft>((draft) => {
  required(draft.name);
  required(draft.addressDescription);
});

/**
 * Fatura hesabı oluşturma/düzenleme formu. `account` verildiğinde alanlar o hesabın
 * değerleriyle dolar ve başlık "Düzenle" olur; verilmediğinde boş "Oluştur" formu açılır.
 */
@Component({
  selector: 'app-customer-account-form',
  imports: [FormField, Button, FormFieldShell, Icon, PanelHeader, CustomerAddressCard],
  host: { class: 'block' },
  template: `
    <app-panel-header [heading]="heading()" />

    <form (submit)="submit($event)">
      <div class="mt-6 flex flex-col gap-5">
        <app-form-field
          for="account-name"
          [label]="t().customers.detail.accounts.accountName"
          [required]="true"
        >
          <input
            id="account-name"
            type="text"
            class="field-control"
            [formField]="accountForm.name"
            aria-required="true"
          />
        </app-form-field>

        <app-form-field
          for="account-address-description"
          [label]="t().customers.detail.accounts.addressDescription"
          [required]="true"
        >
          <textarea
            id="account-address-description"
            rows="3"
            class="field-control resize-y"
            [formField]="accountForm.addressDescription"
            aria-required="true"
          ></textarea>
        </app-form-field>
      </div>

      <div class="mt-6 border-t border-slate-100 pt-5">
        <p class="text-sm font-semibold text-etiya-navy">
          {{ t().customers.detail.accounts.addressInfo }}
        </p>

        <div class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          @for (address of addresses(); track address.id) {
            <app-customer-address-card
              [address]="address"
              [showRadio]="true"
              [showMenu]="false"
              [isPrimary]="selectedAddressId() === address.id"
              (primarySelected)="selectedAddressId.set($event)"
            />
          }

          <button
            type="button"
            class="flex min-h-35 flex-col items-center justify-center gap-3 rounded-xl border border-dashed border-slate-300 text-sm font-semibold text-etiya-navy transition hover:border-etiya-orange hover:bg-etiya-orange/5 focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
          >
            <span
              aria-hidden="true"
              class="flex h-10 w-10 items-center justify-center rounded-full bg-etiya-orange/10 text-etiya-orange"
            >
              <app-icon name="plus" size="h-5 w-5" [stroke]="1.8" />
            </span>
            {{ t().customers.detail.accounts.addAddress }}
          </button>
        </div>
      </div>

      <div class="mt-8 flex items-center justify-between gap-3 border-t border-slate-100 pt-5">
        <app-button type="button" variant="outline" size="lg" (click)="cancelled.emit()">
          {{ t().customers.detail.accounts.formCancel }}
        </app-button>

        <app-button type="submit" size="lg" [disabled]="accountForm().invalid()">
          {{ submitLabel() }}
        </app-button>
      </div>
    </form>
  `
})
export class CustomerAccountForm {
  protected readonly t = inject(I18nService).t;

  /** Düzenlenecek hesap; `null` ise yeni hesap oluşturma modudur. */
  readonly account = input<CustomerAccount | null>(null);
  readonly addresses = input.required<readonly CustomerAddress[]>();

  readonly saved = output<AccountFormResult>();
  readonly cancelled = output<void>();

  protected readonly heading = computed(() =>
    this.account() === null
      ? this.t().customers.detail.accounts.createTitle
      : this.t().customers.detail.accounts.editTitle
  );

  protected readonly submitLabel = computed(() =>
    this.account() === null
      ? this.t().customers.detail.accounts.formCreate
      : this.t().customers.detail.accounts.formSave
  );

  /** Düzenlenen hesap geldiğinde ad alanı onun değerine dolar. */
  private readonly draft = linkedSignal<CustomerAccount | null, AccountDraft>({
    source: this.account,
    computation: (account) => ({ name: account?.name ?? '', addressDescription: '' })
  });

  protected readonly accountForm = form(this.draft, accountDraftSchema);

  /** Müşterinin birincil adresi varsayılan seçili gelir. */
  protected readonly selectedAddressId = linkedSignal<
    readonly CustomerAddress[],
    string | null
  >({
    source: this.addresses,
    computation: (addresses) => addresses.find((address) => address.isPrimary)?.id ?? null
  });

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.accountForm().invalid()) {
      return;
    }

    const draft = this.draft();
    this.saved.emit({
      name: draft.name.trim(),
      addressDescription: draft.addressDescription.trim(),
      addressId: this.selectedAddressId()
    });
  }
}
