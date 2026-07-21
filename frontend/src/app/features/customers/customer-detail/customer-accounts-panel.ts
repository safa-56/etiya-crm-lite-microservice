import { Component, computed, inject, input, linkedSignal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { Icon } from '../../../shared/ui/icon/icon';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { Pagination } from '../../../shared/ui/pagination/pagination';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { StatusBadge } from '../../../shared/ui/status-badge/status-badge';
import { CustomerAccount } from '../customer.model';
import { CustomerAccountProducts } from './customer-account-products';

/** Hesap tablosunda bir sayfada gösterilecek hesap sayısı. */
const PAGE_SIZE = 4;

/** "Müşteri Hesabı" sekmesi: sayfalanan hesap tablosu ve açılır ürün listesi. */
@Component({
  selector: 'app-customer-accounts-panel',
  imports: [
    Button,
    EmptyState,
    Icon,
    IconButton,
    Pagination,
    PanelHeader,
    StatusBadge,
    CustomerAccountProducts
  ],
  host: {
    role: 'tabpanel',
    id: 'panel-account',
    'aria-labelledby': 'tab-account',
    tabindex: '0',
    class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm'
  },
  templateUrl: './customer-accounts-panel.html'
})
export class CustomerAccountsPanel {
  protected readonly t = inject(I18nService).t;

  readonly accounts = input.required<readonly CustomerAccount[]>();

  /** Başka bir müşteriye geçildiğinde sayfa başa döner. */
  protected readonly page = linkedSignal<readonly CustomerAccount[], number>({
    source: this.accounts,
    computation: () => 1
  });

  protected readonly accountPages = computed(() => {
    const pageCount = Math.ceil(this.accounts().length / PAGE_SIZE);
    return Array.from({ length: pageCount }, (_, index) => index + 1);
  });

  protected readonly visibleAccounts = computed(() => {
    const start = (this.page() - 1) * PAGE_SIZE;
    return this.accounts().slice(start, start + PAGE_SIZE);
  });

  /** Tasarımda olduğu gibi her sayfanın ilk hesabı açık gelir. */
  protected readonly expandedAccount = linkedSignal<readonly CustomerAccount[], string | null>({
    source: this.visibleAccounts,
    computation: (accounts) => accounts[0]?.number ?? null
  });

  protected toggleAccount(accountNumber: string): void {
    this.expandedAccount.update((current) => (current === accountNumber ? null : accountNumber));
  }
}
