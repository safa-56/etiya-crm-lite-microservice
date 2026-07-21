import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Avatar } from '../../../shared/ui/avatar/avatar';
import { StatusBadge } from '../../../shared/ui/status-badge/status-badge';
import { Customer, customerDisplayName, customerInitials } from '../customer.model';

/** Arama sonuç tablosu; satırdaki ad müşteri detayına götürür. */
@Component({
  selector: 'app-customer-results-table',
  imports: [RouterLink, Avatar, StatusBadge],
  host: { class: 'block overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm' },
  template: `
    <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
      <h2 class="text-base font-bold text-etiya-navy">{{ t().customers.results.title }}</h2>
      <span class="text-xs font-medium text-slate-400">
        {{ customers().length }} {{ t().customers.results.count }}
      </span>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full min-w-180 text-left text-sm">
        <thead class="bg-etiya-gray text-xs font-semibold tracking-wide text-slate-500 uppercase">
          <tr>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.customerId }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.customer }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.identity }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.gsm }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.city }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.status }}</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          @for (customer of customers(); track customer.id) {
            <tr class="transition hover:bg-etiya-gray/70">
              <td class="px-6 py-3 font-medium text-slate-500">{{ customer.code }}</td>
              <td class="px-6 py-3">
                <a
                  [routerLink]="['/customers', customer.id]"
                  [attr.aria-label]="
                    t().customers.results.openDetail + ': ' + displayName(customer)
                  "
                  class="flex items-center gap-3 text-left focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
                >
                  <app-avatar [initials]="initials(customer)" size="sm" tone="soft" />
                  <span class="font-semibold text-etiya-navy hover:text-etiya-orange">
                    {{ displayName(customer) }}
                  </span>
                </a>
              </td>
              <td class="px-6 py-3 text-slate-600">{{ customer.identityNumber }}</td>
              <td class="px-6 py-3 text-slate-600">{{ customer.gsm }}</td>
              <td class="px-6 py-3 text-slate-600">{{ customer.city }}</td>
              <td class="px-6 py-3"><app-status-badge [status]="customer.status" /></td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class CustomerResultsTable {
  protected readonly t = inject(I18nService).t;

  readonly customers = input.required<readonly Customer[]>();

  protected readonly displayName = customerDisplayName;
  protected readonly initials = customerInitials;
}
