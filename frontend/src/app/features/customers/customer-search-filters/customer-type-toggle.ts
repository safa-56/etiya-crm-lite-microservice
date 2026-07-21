import { Component, inject, input, output } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { CustomerType } from '../customer.model';

const BASE =
  'rounded-md py-2 text-sm font-semibold transition focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange';
const SELECTED = 'bg-etiya-orange text-white shadow-sm';
const IDLE = 'text-slate-500 hover:text-etiya-navy';

/** Bireysel / kurumsal müşteri seçimi. */
@Component({
  selector: 'app-customer-type-toggle',
  host: {
    role: 'group',
    class: 'grid grid-cols-2 gap-1 rounded-lg bg-etiya-gray p-1',
    '[attr.aria-label]': 't().customers.filters.customerType'
  },
  template: `
    <button
      type="button"
      (click)="valueChange.emit('B2C')"
      [attr.aria-pressed]="value() === 'B2C'"
      [class]="classes('B2C')"
    >
      {{ t().customers.filters.b2c }}
    </button>
    <button
      type="button"
      (click)="valueChange.emit('B2B')"
      [attr.aria-pressed]="value() === 'B2B'"
      [class]="classes('B2B')"
    >
      {{ t().customers.filters.b2b }}
    </button>
  `
})
export class CustomerTypeToggle {
  protected readonly t = inject(I18nService).t;

  readonly value = input.required<CustomerType>();
  readonly valueChange = output<CustomerType>();

  protected classes(type: CustomerType): string {
    return `${BASE} ${this.value() === type ? SELECTED : IDLE}`;
  }
}
