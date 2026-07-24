import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, of, startWith, switchMap } from 'rxjs';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Breadcrumb, BreadcrumbItem } from '../../../shared/ui/breadcrumb/breadcrumb';
import { ButtonLink } from '../../../shared/ui/button/button-link';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { TabItem, Tabs } from '../../../shared/ui/tabs/tabs';
import { Customer } from '../customer.model';
import { CustomerService } from '../customer.service';
import { CustomerAccountsPanel } from './customer-accounts-panel';
import { CustomerAddressesPanel } from './customer-addresses-panel';
import { CustomerContactPanel } from './customer-contact-panel';
import { CustomerDetailHeader } from './customer-detail-header';
import { CustomerInfoPanel } from './customer-info-panel';

type DetailTab = 'info' | 'account' | 'address' | 'contact';

const TAB_ORDER: readonly DetailTab[] = ['info', 'account', 'address', 'contact'];

/** Detay yükleme durumu makinesi: yükleniyor → yüklendi (müşteri veya bulunamadı) / hata. */
type DetailState =
  | { readonly status: 'loading' }
  | { readonly status: 'error' }
  | { readonly status: 'loaded'; readonly customer: Customer | null };

/** Detay sayfası kabuğu: müşteriyi çözer, aktif sekmeyi tutar, panelleri seçer. */
@Component({
  selector: 'app-customer-detail',
  imports: [
    Breadcrumb,
    ButtonLink,
    EmptyState,
    Tabs,
    CustomerDetailHeader,
    CustomerInfoPanel,
    CustomerAccountsPanel,
    CustomerAddressesPanel,
    CustomerContactPanel
  ],
  templateUrl: './customer-detail.html'
})
export class CustomerDetail {
  private readonly customers = inject(CustomerService);

  protected readonly t = inject(I18nService).t;

  /** `customers/:id` route parametresi; component input binding ile bağlanır. */
  readonly id = input.required<string>();

  /**
   * Detay yükleme durumu. Route parametresi değiştikçe BFF'ten yeniden çekilir;
   * 404 "bulunamadı" (null müşteri) olarak, diğer hatalar hata durumu olarak ele alınır.
   */
  private readonly state = toSignal(
    toObservable(this.id).pipe(
      switchMap((id) =>
        this.customers.getDetailById(id).pipe(
          map((customer): DetailState => ({ status: 'loaded', customer })),
          catchError((error: unknown) =>
            of<DetailState>(
              error instanceof HttpErrorResponse && error.status === 404
                ? { status: 'loaded', customer: null }
                : { status: 'error' }
            )
          ),
          startWith<DetailState>({ status: 'loading' })
        )
      )
    ),
    { initialValue: { status: 'loading' } as DetailState }
  );

  protected readonly loading = computed(() => this.state().status === 'loading');

  protected readonly loadError = computed(() => this.state().status === 'error');

  /**
   * Ekranda gösterilen müşteri. İlk yükleme {@link state}'ten seed edilir; bir panel backend'e
   * yazdıktan sonra {@link reload} bu sinyali sessizce (yükleniyor ekranı göstermeden) günceller,
   * böylece başlık ve tüm paneller otoriter veriyle tazelenir.
   */
  protected readonly customer = linkedSignal<DetailState, Customer | null>({
    source: this.state,
    computation: (state) => (state.status === 'loaded' ? state.customer : null)
  });

  /** Başka bir müşteriye geçildiğinde sekme başa döner (yalnızca id değişince). */
  protected readonly activeTab = linkedSignal<string, DetailTab>({
    source: this.id,
    computation: () => 'info'
  });

  /**
   * Bir panel güncelleme yazdıktan sonra detayı BFF'ten yeniden çeker ve görünümü sessizce
   * tazeler. Yükleniyor durumuna geçmez (mevcut veri korunur) ve aktif sekme değişmez.
   */
  protected reload(): void {
    this.customers.getDetailById(this.id()).subscribe({
      next: (customer) => this.customer.set(customer),
      error: () => {
        // Sessiz başarısızlık: kayıt zaten backend'e yazıldı, mevcut görünüm korunur.
      }
    });
  }

  protected readonly tabs = computed<readonly TabItem[]>(() => {
    const labels = this.t().customers.detail.tabs;
    return TAB_ORDER.map((id) => ({ id, label: labels[id] }));
  });

  protected readonly breadcrumb = computed<readonly BreadcrumbItem[]>(() => [
    { label: this.t().nav.customerSearch, link: '/customers' },
    { label: this.t().customers.detail.tabs[this.activeTab()] }
  ]);

  /** `app-tabs` genel amaçlı olduğu için string döner; burada daraltılır. */
  protected selectTab(id: string): void {
    if ((TAB_ORDER as readonly string[]).includes(id)) {
      this.activeTab.set(id as DetailTab);
    }
  }
}
