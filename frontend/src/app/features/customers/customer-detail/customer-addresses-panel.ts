import { Component, inject, input, linkedSignal, signal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerAddress } from '../customer.model';
import { CustomerAddressCard } from './customer-address-card';
import { AddressFormResult, CustomerAddressForm } from './customer-address-form';

/** "Adres" sekmesi: adres kartları, üç nokta menüsü ve ekle/düzenle formu. */
@Component({
  selector: 'app-customer-addresses-panel',
  imports: [Icon, PanelHeader, CustomerAddressCard, CustomerAddressForm],
  host: {
    role: 'tabpanel',
    id: 'panel-address',
    'aria-labelledby': 'tab-address',
    tabindex: '0',
    class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm'
  },
  template: `
    @if (mode() === 'form') {
      <app-customer-address-form
        [address]="editingAddress()"
        (saved)="saveAddress($event)"
        (cancelled)="closeForm()"
      />
    } @else {
      <app-panel-header [heading]="t().customers.detail.addresses.title" />

      <div class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        @for (address of addressList(); track address.id) {
          <app-customer-address-card
            [address]="address"
            [canDelete]="addressList().length > 1"
            [showRadio]="true"
            [isPrimary]="primaryAddressId() === address.id"
            (primarySelected)="primaryAddressId.set($event)"
            (edit)="openEdit(address)"
            (delete)="deleteAddress(address)"
          />
        }

        <button
          type="button"
          (click)="openCreate()"
          class="flex min-h-35 flex-col items-center justify-center gap-3 rounded-xl border border-dashed border-slate-300 text-sm font-semibold text-etiya-navy transition hover:border-etiya-orange hover:bg-etiya-orange/5 focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
        >
          <span
            aria-hidden="true"
            class="flex h-10 w-10 items-center justify-center rounded-full bg-etiya-orange/10 text-etiya-orange"
          >
            <app-icon name="plus" size="h-5 w-5" [stroke]="1.8" />
          </span>
          {{ t().customers.detail.addresses.add }}
        </button>
      </div>
    }
  `
})
export class CustomerAddressesPanel {
  protected readonly t = inject(I18nService).t;

  readonly addresses = input.required<readonly CustomerAddress[]>();

  /** Ekle/düzenle/sil işlemleri bu yerel liste üzerinde uygulanır. */
  protected readonly addressList = linkedSignal<readonly CustomerAddress[], CustomerAddress[]>({
    source: this.addresses,
    computation: (addresses) => [...addresses]
  });

  /**
   * Birincil adres seçimi; varsayılan olarak hiçbir adres seçili gelmez, kullanıcı radyo
   * düğmesiyle kendisi seçer. Seçili adres silinirse seçim boşa döner.
   */
  protected readonly primaryAddressId = linkedSignal<CustomerAddress[], string | null>({
    source: this.addressList,
    computation: (addresses, previous) => {
      const previousId = previous?.value ?? null;
      return previousId !== null && addresses.some((address) => address.id === previousId)
        ? previousId
        : null;
    }
  });

  /** 'list' kart görünümü, 'form' ekle/düzenle formu. */
  protected readonly mode = signal<'list' | 'form'>('list');

  /** Formdaki adres; `null` ise yeni adres ekleniyor demektir. */
  protected readonly editingAddress = signal<CustomerAddress | null>(null);

  protected openCreate(): void {
    this.editingAddress.set(null);
    this.mode.set('form');
  }

  protected openEdit(address: CustomerAddress): void {
    this.editingAddress.set(address);
    this.mode.set('form');
  }

  protected closeForm(): void {
    this.mode.set('list');
    this.editingAddress.set(null);
  }

  protected saveAddress(result: AddressFormResult): void {
    const title = `${result.city}, ${result.street}, ${result.buildingNo}`;
    const editing = this.editingAddress();

    if (editing === null) {
      const address: CustomerAddress = {
        id: String(Date.now()),
        title,
        detail: result.description,
        isPrimary: this.addressList().length === 0
      };
      this.addressList.update((addresses) => [...addresses, address]);
    } else {
      this.addressList.update((addresses) =>
        addresses.map((address) =>
          address.id === editing.id ? { ...address, title, detail: result.description } : address
        )
      );
    }

    this.closeForm();
  }

  protected deleteAddress(address: CustomerAddress): void {
    if (this.addressList().length <= 1) {
      return;
    }

    this.addressList.update((addresses) => addresses.filter((item) => item.id !== address.id));
  }
}
