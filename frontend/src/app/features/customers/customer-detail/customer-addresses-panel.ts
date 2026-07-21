import { Component, inject, input, linkedSignal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerAddress } from '../customer.model';
import { CustomerAddressCard } from './customer-address-card';

/** "Adres" sekmesi: adres kartları ve yeni adres ekleme alanı. */
@Component({
  selector: 'app-customer-addresses-panel',
  imports: [Icon, PanelHeader, CustomerAddressCard],
  host: {
    role: 'tabpanel',
    id: 'panel-address',
    'aria-labelledby': 'tab-address',
    tabindex: '0',
    class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm'
  },
  template: `
    <app-panel-header [heading]="t().customers.detail.addresses.title" />

    <div class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      @for (address of addresses(); track address.id) {
        <app-customer-address-card
          [address]="address"
          [isPrimary]="primaryId() === address.id"
          (primarySelected)="primaryId.set($event)"
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
        {{ t().customers.detail.addresses.add }}
      </button>
    </div>
  `
})
export class CustomerAddressesPanel {
  protected readonly t = inject(I18nService).t;

  readonly addresses = input.required<readonly CustomerAddress[]>();

  /** Müşteri değiştiğinde seçim, o müşterinin varsayılan adresine döner. */
  protected readonly primaryId = linkedSignal<readonly CustomerAddress[], string | null>({
    source: this.addresses,
    computation: (addresses) => addresses.find((address) => address.isPrimary)?.id ?? null
  });
}
