import { Component, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { FormField, form, required, schema } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerAccount, CustomerAddress } from '../customer.model';
import { CustomerAddressCard } from './customer-address-card';
import { AddressFormResult, CustomerAddressForm } from './customer-address-form';

/** Formun ürettiği sonuç; hesap panelinde listeye işlenir. */
export interface AccountFormResult {
  name: string;
  accountDescription: string;
  addressId: string | null;
}

interface AccountDraft {
  name: string;
  accountDescription: string;
}

const accountDraftSchema = schema<AccountDraft>((draft) => {
  required(draft.name);
  required(draft.accountDescription);
});

/**
 * Fatura hesabı oluşturma/düzenleme formu. `account` verildiğinde alanlar o hesabın
 * değerleriyle dolar ve başlık "Düzenle" olur; verilmediğinde boş "Oluştur" formu açılır.
 */
@Component({
  selector: 'app-customer-account-form',
  imports: [
    FormField,
    Button,
    FormFieldShell,
    Icon,
    PanelHeader,
    CustomerAddressCard,
    CustomerAddressForm
  ],
  host: { class: 'block' },
  template: `
    @if (addingAddress()) {
      <app-customer-address-form
        (saved)="saveNewAddress($event)"
        (cancelled)="cancelAddAddress()"
      />
    } @else {
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
            for="account-description"
            [label]="t().customers.detail.accounts.accountDescription"
            [required]="true"
          >
            <textarea
              id="account-description"
              rows="3"
              class="field-control resize-y"
              [formField]="accountForm.accountDescription"
              aria-required="true"
            ></textarea>
          </app-form-field>
        </div>

        <div class="mt-6 border-t border-slate-100 pt-5">
          <p class="text-sm font-semibold text-etiya-navy">
            {{ t().customers.detail.accounts.addressInfo }}
          </p>

          <div class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            @for (address of addressList(); track address.id) {
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
              (click)="openAddAddress()"
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

        @if (saveError()) {
          <p class="mt-5 text-sm font-medium text-red-600" role="alert">
            {{ t().customers.detail.accounts.saveError }}
          </p>
        }

        <div class="mt-8 flex items-center justify-between gap-3 border-t border-slate-100 pt-5">
          <app-button type="button" variant="outline" size="lg" (click)="cancelled.emit()">
            {{ t().customers.detail.accounts.formCancel }}
          </app-button>

          <app-button type="submit" size="lg" [disabled]="accountForm().invalid() || saving()">
            {{ submitLabel() }}
          </app-button>
        </div>
      </form>
    }
  `
})
export class CustomerAccountForm {
  protected readonly t = inject(I18nService).t;

  /** Düzenlenecek hesap; `null` ise yeni hesap oluşturma modudur. */
  readonly account = input<CustomerAccount | null>(null);
  readonly addresses = input.required<readonly CustomerAddress[]>();

  /** Üst panel backend'e yazarken düğmeyi kilitler. */
  readonly saving = input(false);

  /** Üst panelde backend hatası oluştuğunda uyarı gösterilir. */
  readonly saveError = input(false);

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

  /** Düzenlenen hesap geldiğinde ad ve açıklama alanları onun değerlerine dolar. */
  private readonly draft = linkedSignal<CustomerAccount | null, AccountDraft>({
    source: this.account,
    computation: (account) => ({
      name: account?.name ?? '',
      accountDescription: account?.accountDescription ?? ''
    })
  });

  protected readonly accountForm = form(this.draft, accountDraftSchema);

  /** Form açıkken eklenen yeni adresler de bu listeye yazılır. */
  protected readonly addressList = linkedSignal<readonly CustomerAddress[], CustomerAddress[]>({
    source: this.addresses,
    computation: (addresses) => [...addresses]
  });

  /**
   * Seçili adres. Oluşturmada varsayılan seçili gelmez; düzenlemede hesabın mevcut adresi
   * (varsa) ön-seçili gelir. Kullanıcı radyo düğmesiyle değiştirebilir.
   */
  protected readonly selectedAddressId = linkedSignal<readonly CustomerAddress[], string | null>({
    source: this.addressList,
    computation: (addresses, previous) => {
      const previousId = previous?.value ?? null;
      if (previousId !== null && addresses.some((address) => address.id === previousId)) {
        return previousId;
      }

      const accountAddressId = this.account()?.addressId ?? null;
      if (accountAddressId !== null) {
        const match = String(accountAddressId);
        if (addresses.some((address) => address.id === match)) {
          return match;
        }
      }

      // Oluşturmada (veya hesabın adresi çözülemezse) birincil adres, yoksa ilk adres seçili gelir.
      const primary = addresses.find((address) => address.isPrimary);
      return primary?.id ?? addresses[0]?.id ?? null;
    }
  });

  /** Yeni adres ekleme alt-modu açık mı? */
  protected readonly addingAddress = signal(false);

  protected openAddAddress(): void {
    this.addingAddress.set(true);
  }

  protected cancelAddAddress(): void {
    this.addingAddress.set(false);
  }

  /** Yeni adresi listeye ekler, seçili yapar ve hesap formuna geri döner. */
  protected saveNewAddress(result: AddressFormResult): void {
    const address: CustomerAddress = {
      id: String(Date.now()),
      title: `${result.city}, ${result.street}, ${result.buildingNo}`,
      detail: result.description,
      isPrimary: false
    };

    this.addressList.update((addresses) => [...addresses, address]);
    this.selectedAddressId.set(address.id);
    this.addingAddress.set(false);
  }

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.accountForm().invalid()) {
      return;
    }

    const draft = this.draft();
    this.saved.emit({
      name: draft.name.trim(),
      accountDescription: draft.accountDescription.trim(),
      addressId: this.selectedAddressId()
    });
  }
}
