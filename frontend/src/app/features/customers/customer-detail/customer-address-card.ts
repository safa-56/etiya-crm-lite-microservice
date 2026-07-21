import { Component, inject, input, output } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Icon } from '../../../shared/ui/icon/icon';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { CustomerAddress } from '../customer.model';

/**
 * Tek adres kartı. Radyo düğmeleri `name` üzerinden gruplandığı için karta
 * `role="radiogroup"` verilmez.
 */
@Component({
  selector: 'app-customer-address-card',
  imports: [Icon, IconButton],
  host: { class: 'flex min-h-35 flex-col rounded-xl border border-slate-200 p-4' },
  template: `
    <div class="flex items-start justify-between">
      <span class="text-etiya-orange"><app-icon name="pin" size="h-5 w-5" /></span>
      <app-icon-button
        icon="dots"
        tone="ghost"
        [label]="t().customers.detail.addresses.menu + ': ' + address().title"
      />
    </div>

    <p class="mt-3 text-sm font-bold text-etiya-navy">{{ address().title }}</p>
    <p class="mt-1 text-sm text-slate-500">{{ address().detail }}</p>

    <div class="mt-auto flex justify-end pt-3">
      <input
        type="radio"
        name="primaryAddress"
        [value]="address().id"
        [checked]="isPrimary()"
        (change)="primarySelected.emit(address().id)"
        [attr.aria-label]="t().customers.detail.addresses.primary + ': ' + address().title"
        class="h-5 w-5 accent-etiya-orange focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
      />
    </div>
  `
})
export class CustomerAddressCard {
  protected readonly t = inject(I18nService).t;

  readonly address = input.required<CustomerAddress>();
  readonly isPrimary = input.required<boolean>();

  readonly primarySelected = output<string>();
}
