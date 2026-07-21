import { Component, computed, inject, input, linkedSignal } from '@angular/core';

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

  protected readonly customer = computed<Customer | null>(() => this.customers.getById(this.id()));

  /** Başka bir müşteriye geçildiğinde sekme başa döner. */
  protected readonly activeTab = linkedSignal<Customer | null, DetailTab>({
    source: this.customer,
    computation: () => 'info'
  });

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
