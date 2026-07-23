import { Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { CustomerAccountProduct } from '../customer.model';
import { CustomerProductPreview } from './customer-product-preview';

/** Hesap satırı açıldığında görünen ürün tablosu ve hesap aksiyonları. */
@Component({
  selector: 'app-customer-account-products',
  imports: [Button, EmptyState, IconButton, CustomerProductPreview],
  host: { class: 'block' },
  template: `
    @if (products().length > 0) {
      <div class="overflow-hidden rounded-lg border border-slate-200">
        <table class="w-full text-left text-sm">
          <thead class="bg-etiya-gray text-xs font-semibold tracking-wide text-slate-500 uppercase">
            <tr>
              <th scope="col" class="px-5 py-2.5">
                {{ t().customers.detail.accounts.productId }}
              </th>
              <th scope="col" class="px-5 py-2.5">
                {{ t().customers.detail.accounts.productName }}
              </th>
              <th scope="col" class="px-5 py-2.5">
                {{ t().customers.detail.accounts.campaignName }}
              </th>
              <th scope="col" class="px-5 py-2.5">
                {{ t().customers.detail.accounts.campaignId }}
              </th>
              <th scope="col" class="px-5 py-2.5 text-right">
                {{ t().customers.detail.accounts.actions }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 bg-white">
            @for (product of products(); track product.id) {
              <tr>
                <td class="px-5 py-3 text-slate-600">{{ product.id }}</td>
                <td class="px-5 py-3 font-semibold text-etiya-navy">{{ product.name }}</td>
                <td class="px-5 py-3 text-slate-600">
                  {{ product.campaignName ?? t().common.empty }}
                </td>
                <td class="px-5 py-3 text-slate-600">
                  {{ product.campaignId ?? t().common.empty }}
                </td>
                <td class="px-5 py-3">
                  <div class="flex items-center justify-end gap-2">
                    <app-icon-button
                      icon="trash"
                      tone="danger"
                      [label]="t().customers.detail.accounts.productDelete + ': ' + product.name"
                    />
                    <app-icon-button
                      icon="eye"
                      tone="neutral"
                      [label]="t().customers.detail.accounts.productPreview + ': ' + product.name"
                      (click)="openPreview(product)"
                    />
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    } @else {
      <app-empty-state size="sm" [message]="t().customers.detail.accounts.noProducts" />
    }

    @if (previewProduct(); as product) {
      <app-customer-product-preview [product]="product" (closed)="closePreview()" />
    }

    <div class="mt-4 flex flex-wrap gap-3">
      <app-button variant="secondary" size="sm" (click)="startNewSale()">
        {{ t().customers.detail.accounts.newSale }}
      </app-button>
      <app-button variant="secondary" size="sm">
        {{ t().customers.detail.accounts.transfer }}
      </app-button>
      <app-button variant="secondary" size="sm">
        {{ t().customers.detail.accounts.changeServiceAddress }}
      </app-button>
    </div>
  `
})
export class CustomerAccountProducts {
  private readonly router = inject(Router);

  protected readonly t = inject(I18nService).t;

  readonly customerId = input.required<number>();
  readonly products = input.required<readonly CustomerAccountProduct[]>();

  /** Önizlemesi açık olan ürün; `null` ise pencere kapalıdır. */
  protected readonly previewProduct = signal<CustomerAccountProduct | null>(null);

  protected openPreview(product: CustomerAccountProduct): void {
    this.previewProduct.set(product);
  }

  protected closePreview(): void {
    this.previewProduct.set(null);
  }

  /** "Yeni Satış Başlat": teklif seçimiyle başlayan satış akışına götürür. */
  protected startNewSale(): void {
    void this.router.navigate(['/customers', this.customerId(), 'new-sale']);
  }
}
