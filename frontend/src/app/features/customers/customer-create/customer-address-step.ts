import { Component, inject, model, output, signal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerAddress } from '../customer.model';
import { CustomerAddressCard } from '../customer-detail/customer-address-card';
import {
  AddressFormResult,
  CustomerAddressForm
} from '../customer-detail/customer-address-form';

/**
 * Sihirbazın 2. adımı: müşteriye eklenecek adresleri toplar. Kart listesi ile "Yeni Adres Ekle"
 * arasında geçiş yapar; en az bir adres eklenmeden İleri etkinleşmez.
 */
@Component({
  selector: 'app-customer-address-step',
  imports: [Button, Icon, PanelHeader, CustomerAddressCard, CustomerAddressForm],
  host: { class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm' },
  template: `
    <app-panel-header [heading]="t().customers.create.steps.address" />

    @if (mode() === 'form') {
      <app-customer-address-form
        [address]="editingAddress()"
        (saved)="saveAddress($event)"
        (cancelled)="closeForm()"
      />
    } @else {
      <div class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        @for (address of addresses(); track address.id) {
          <app-customer-address-card
            [address]="address"
            [canDelete]="true"
            [showRadio]="true"
            [isPrimary]="address.isPrimary"
            (primarySelected)="setPrimary($event)"
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

      <div class="mt-8 flex items-center justify-between gap-3 border-t border-slate-100 pt-5">
        <app-button variant="outline" size="lg" (click)="back.emit()">
          <app-icon name="arrow-left" [stroke]="1.8" />
          {{ t().customers.create.back }}
        </app-button>

        <app-button size="lg" [disabled]="addresses().length === 0" (click)="next.emit()">
          {{ t().customers.create.next }}
          <app-icon name="arrow-right" [stroke]="1.8" />
        </app-button>
      </div>
    }
  `
})
export class CustomerAddressStep {
  protected readonly t = inject(I18nService).t;

  /** İki yönlü bağlanır; sihirbaz adımlar arası geçişte listeyi korur. */
  readonly addresses = model.required<CustomerAddress[]>();

  readonly back = output<void>();
  readonly next = output<void>();

  protected readonly mode = signal<'list' | 'form'>('list');
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
        isPrimary: this.addresses().length === 0,
        city: result.city,
        street: result.street,
        houseNumber: result.buildingNo
      };
      this.addresses.update((addresses) => [...addresses, address]);
    } else {
      this.addresses.update((addresses) =>
        addresses.map((address) =>
          address.id === editing.id
            ? {
                ...address,
                title,
                detail: result.description,
                city: result.city,
                street: result.street,
                houseNumber: result.buildingNo
              }
            : address
        )
      );
    }

    this.closeForm();
  }

  /** Seçilen adresi birincil yapar; diğerlerinin birincil işaretini kaldırır. */
  protected setPrimary(id: string): void {
    this.addresses.update((addresses) =>
      addresses.map((address) => ({ ...address, isPrimary: address.id === id }))
    );
  }

  protected deleteAddress(address: CustomerAddress): void {
    this.addresses.update((addresses) => {
      const remaining = addresses.filter((item) => item.id !== address.id);
      // Birincil adres silindiyse ilk kalan adres birincil olur (en az biri birincil kalsın).
      if (address.isPrimary && remaining.length > 0 && !remaining.some((item) => item.isPrimary)) {
        return remaining.map((item, index) => ({ ...item, isPrimary: index === 0 }));
      }
      return remaining;
    });
  }
}
