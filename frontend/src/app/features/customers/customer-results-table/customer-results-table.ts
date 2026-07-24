import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { CustomerSearchResult } from '../customer.service';
import { Icon } from '../../../shared/ui/icon/icon';

/** Sayfalamada gösterilen bir öğe: ya sayfa numarası ya da atlanan aralık ('gap'). */
type PageItem = number | 'gap';

/** Arama sonuç tablosu; satırdaki ad müşteri detayına götürür, altında sayfalama sunar. */
@Component({
  selector: 'app-customer-results-table',
  imports: [RouterLink, Icon],
  host: { class: 'block overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm' },
  template: `
    <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
      <h2 class="text-base font-bold text-etiya-navy">{{ t().customers.results.title }}</h2>
      <span
        class="rounded-full bg-etiya-gray px-3 py-1 text-xs font-semibold text-slate-500"
      >
        {{ result().totalElements }} {{ t().customers.results.countBadge }}
      </span>
    </div>

    @if (showPagedInfo()) {
      <div class="px-6 pt-4">
        <p
          class="flex items-center gap-2 rounded-xl bg-sky-50 px-4 py-3 text-sm text-sky-800"
          role="status"
        >
          <span class="text-sky-500"><app-icon name="info" size="h-5 w-5" [stroke]="1.8" /></span>
          {{ pagedInfo() }}
        </p>
      </div>
    }

    <div class="overflow-x-auto">
      <table class="w-full min-w-200 text-left text-sm">
        <thead class="bg-etiya-gray text-xs font-semibold tracking-wide text-slate-500 uppercase">
          <tr>
            <th scope="col" class="px-6 py-3">
              <span class="inline-flex items-center gap-1">
                {{ t().customers.results.customerId }}
                <span aria-hidden="true" class="text-etiya-orange">▲</span>
              </span>
            </th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.firstName }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.secondName }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.lastName }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.role }}</th>
            <th scope="col" class="px-6 py-3">{{ t().customers.results.identity }}</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          @for (row of result().rows; track row.customerId) {
            <tr class="transition hover:bg-etiya-gray/70">
              <td class="px-6 py-3">
                <a
                  [routerLink]="['/customers', row.customerId]"
                  [attr.aria-label]="t().customers.results.openDetail + ': ' + row.customerId"
                  class="font-semibold text-etiya-navy hover:text-etiya-orange focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
                >
                  {{ row.customerId }}
                </a>
              </td>
              <td class="px-6 py-3 text-slate-700">{{ row.firstName }}</td>
              <td class="px-6 py-3 text-slate-500">{{ row.secondName || dash }}</td>
              <td class="px-6 py-3 text-slate-700">{{ row.lastName }}</td>
              <td class="px-6 py-3">
                <span
                  class="inline-flex rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700"
                >
                  {{ t().customers.results.roleLabels[roleKey(row.role)] }}
                </span>
              </td>
              <td class="px-6 py-3 text-slate-600">{{ row.nationalityId }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <div
      class="flex flex-col gap-3 border-t border-slate-100 px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
    >
      <p class="text-xs font-medium text-slate-500">{{ rangeText() }}</p>

      @if (pageItems().length > 0) {
        <nav [attr.aria-label]="t().customers.results.pageNav">
          <ul class="flex items-center gap-1.5">
            <li>
              <button
                type="button"
                (click)="goTo(currentPage() - 1)"
                [disabled]="currentPage() <= 1"
                [attr.aria-label]="t().customers.results.prevPage"
                class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-etiya-navy transition hover:bg-etiya-gray focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <span class="block rotate-180"
                  ><app-icon name="chevron-right" size="h-4 w-4" [stroke]="2"
                /></span>
              </button>
            </li>

            @for (item of pageItems(); track $index) {
              <li>
                @if (item === 'gap') {
                  <span class="flex h-9 w-9 items-center justify-center text-slate-400">…</span>
                } @else {
                  <button
                    type="button"
                    (click)="goTo(item)"
                    [attr.aria-current]="currentPage() === item ? 'page' : null"
                    [attr.aria-label]="t().customers.results.pageLabel + ' ' + item"
                    class="flex h-9 w-9 items-center justify-center rounded-lg border text-sm font-semibold transition focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
                    [class]="
                      currentPage() === item
                        ? 'border-etiya-orange bg-etiya-orange text-white'
                        : 'border-slate-200 bg-white text-etiya-navy hover:bg-etiya-gray'
                    "
                  >
                    {{ item }}
                  </button>
                }
              </li>
            }

            <li>
              <button
                type="button"
                (click)="goTo(currentPage() + 1)"
                [disabled]="result().last"
                [attr.aria-label]="t().customers.results.nextPage"
                class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-etiya-navy transition hover:bg-etiya-gray focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <app-icon name="chevron-right" size="h-4 w-4" [stroke]="2" />
              </button>
            </li>
          </ul>
        </nav>
      }
    </div>
  `
})
export class CustomerResultsTable {
  protected readonly t = inject(I18nService).t;

  readonly result = input.required<CustomerSearchResult>();

  /** İstenen 1 tabanlı sayfa; bileşen kendisi veri çekmez, üst bileşene devreder. */
  readonly pageChange = output<number>();

  protected readonly dash = '—';

  /** Toplam sonuç, tek sayfada sığmayacak kadar çoksa bilgilendirme şeridi gösterilir. */
  protected readonly showPagedInfo = computed(() => this.result().totalElements > 50);

  protected readonly pagedInfo = computed(() =>
    this.t().customers.results.pagedInfo.replace('{count}', String(this.result().totalElements))
  );

  /** Görünen 1 tabanlı sayfa numarası (backend 0 tabanlı döndürür). */
  protected readonly currentPage = computed(() => this.result().pageNumber + 1);

  protected readonly rangeText = computed(() => {
    const { pageNumber, pageSize, totalElements } = this.result();
    if (totalElements === 0) {
      return this.t()
        .customers.results.range.replace('{total}', '0')
        .replace('{from}', '0')
        .replace('{to}', '0');
    }
    const from = pageNumber * pageSize + 1;
    const to = Math.min(from + pageSize - 1, totalElements);
    return this.t()
      .customers.results.range.replace('{total}', String(totalElements))
      .replace('{from}', String(from))
      .replace('{to}', String(to));
  });

  /**
   * Görünecek sayfa numaraları: ilk, son ve geçerli sayfanın komşuları; aradaki
   * boşluklar 'gap' ile temsil edilir. Tek sayfada boş döner (gezinme gizlenir).
   */
  protected readonly pageItems = computed<readonly PageItem[]>(() => {
    const total = this.result().totalPages;
    if (total <= 1) {
      return [];
    }

    const current = this.currentPage();
    const wanted = new Set<number>([1, total, current - 1, current, current + 1]);
    const sorted = [...wanted].filter((page) => page >= 1 && page <= total).sort((a, b) => a - b);

    const items: PageItem[] = [];
    let previous = 0;
    for (const page of sorted) {
      if (page - previous > 1) {
        items.push('gap');
      }
      items.push(page);
      previous = page;
    }
    return items;
  });

  /** Rol enum'unu i18n rol etiketi anahtarına eşler. */
  protected roleKey(role: CustomerSearchResult['rows'][number]['role']): 'b2c' | 'b2b' {
    return role === 'B2B' ? 'b2b' : 'b2c';
  }

  protected goTo(page: number): void {
    if (page < 1 || page > this.result().totalPages || page === this.currentPage()) {
      return;
    }
    // Üst bileşen 0 tabanlı bekler.
    this.pageChange.emit(page - 1);
  }
}
