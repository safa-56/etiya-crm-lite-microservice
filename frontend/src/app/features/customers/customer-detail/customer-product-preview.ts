import {
  Component,
  ElementRef,
  afterNextRender,
  computed,
  inject,
  input,
  output
} from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { CustomerAccountProduct } from '../customer.model';

/**
 * Ürün önizleme penceresi. Hesap ürün tablosundaki göz düğmesiyle açılır; teklif/spec
 * bilgilerini ve varsa hizmet adresini gösterir. Escape veya arka plana tıklama kapatır.
 */
@Component({
  selector: 'app-customer-product-preview',
  imports: [IconButton],
  host: {
    class:
      'fixed inset-0 z-50 flex items-center justify-center bg-etiya-navy/40 px-4 backdrop-blur-sm',
    role: 'presentation',
    '(click)': 'onBackdrop($event)',
    '(keydown.escape)': 'closed.emit()'
  },
  template: `
    <div
      role="dialog"
      aria-modal="true"
      [attr.aria-label]="product().name"
      class="w-full max-w-2xl rounded-2xl bg-white shadow-xl"
    >
      <div class="flex items-center justify-between gap-4 border-b border-slate-100 px-6 py-4">
        <h2 class="text-base font-bold text-etiya-navy">{{ product().name }}</h2>
        <app-icon-button
          icon="close"
          tone="neutral"
          [label]="t().customers.detail.accounts.previewClose"
          (click)="closed.emit()"
        />
      </div>

      @if (preview(); as detail) {
        <div class="px-6 py-5">
          <dl class="grid gap-x-6 gap-y-4 sm:grid-cols-3">
            <div>
              <dt class="text-xs font-semibold tracking-wide text-slate-500 uppercase">
                {{ t().customers.detail.accounts.previewOfferId }}
              </dt>
              <dd class="mt-1 text-sm font-semibold text-etiya-navy">{{ detail.offerId }}</dd>
            </div>
            <div>
              <dt class="text-xs font-semibold tracking-wide text-slate-500 uppercase">
                {{ t().customers.detail.accounts.previewOfferName }}
              </dt>
              <dd class="mt-1 text-sm font-semibold text-etiya-navy">{{ detail.offerName }}</dd>
            </div>
            <div>
              <dt class="text-xs font-semibold tracking-wide text-slate-500 uppercase">
                {{ t().customers.detail.accounts.previewSpecId }}
              </dt>
              <dd class="mt-1 text-sm font-semibold text-etiya-navy">{{ detail.specId }}</dd>
            </div>
          </dl>

          <div class="mt-5 grid gap-5 border-t border-slate-100 pt-5 sm:grid-cols-2">
            <div>
              <p class="text-xs font-semibold tracking-wide text-slate-500 uppercase">
                {{ t().customers.detail.accounts.previewCharacteristics }}
              </p>
              <p class="mt-2 text-sm text-slate-600">{{ detail.characteristics }}</p>
            </div>

            <div class="rounded-xl bg-etiya-gray px-4 py-3">
              @if (detail.address; as address) {
                <p class="text-sm font-bold text-etiya-navy">
                  {{ t().customers.detail.accounts.previewAddressName }}: {{ address.name }}
                </p>
                <p class="mt-1 text-sm text-slate-600">{{ address.title }}</p>
                <p class="text-sm text-slate-600">
                  {{ t().customers.detail.accounts.previewBuildingNo }}: {{ address.buildingNo }}
                </p>
                <p class="mt-1 text-sm text-slate-500">{{ address.description }}</p>
              } @else {
                <p class="text-sm text-slate-500">
                  {{ t().customers.detail.accounts.previewNoAddress }}
                </p>
              }
            </div>
          </div>
        </div>
      } @else {
        <p class="px-6 py-8 text-center text-sm text-slate-500">
          {{ t().customers.detail.accounts.previewEmpty }}
        </p>
      }
    </div>
  `
})
export class CustomerProductPreview {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly t = inject(I18nService).t;

  readonly product = input.required<CustomerAccountProduct>();

  readonly closed = output<void>();

  protected readonly preview = computed(() => this.product().preview);

  constructor() {
    afterNextRender(() => {
      this.host.nativeElement.querySelector<HTMLButtonElement>('button')?.focus();
    });
  }

  /** Yalnızca arka plana (karartma katmanına) tıklanırsa kapatır. */
  protected onBackdrop(event: MouseEvent): void {
    if (event.target === this.host.nativeElement) {
      this.closed.emit();
    }
  }
}
