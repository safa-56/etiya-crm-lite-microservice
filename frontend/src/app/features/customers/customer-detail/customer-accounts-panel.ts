import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { Icon } from '../../../shared/ui/icon/icon';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { Pagination } from '../../../shared/ui/pagination/pagination';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { StatusBadge } from '../../../shared/ui/status-badge/status-badge';
import { CustomerAccount, CustomerAddress } from '../customer.model';
import { CustomerAccountProducts } from './customer-account-products';
import { AccountFormResult, CustomerAccountForm } from './customer-account-form';

/** Hesap tablosunda bir sayfada gösterilecek hesap sayısı. */
const PAGE_SIZE = 4;

/** "Müşteri Hesabı" sekmesi: sayfalanan hesap tablosu ile oluştur/düzenle/sil akışları. */
@Component({
  selector: 'app-customer-accounts-panel',
  imports: [
    Button,
    ConfirmDialog,
    EmptyState,
    Icon,
    IconButton,
    Pagination,
    PanelHeader,
    StatusBadge,
    CustomerAccountProducts,
    CustomerAccountForm
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

  readonly customerId = input.required<number>();
  readonly accounts = input.required<readonly CustomerAccount[]>();
  readonly addresses = input.required<readonly CustomerAddress[]>();

  /** Oluştur/düzenle/sil işlemleri bu yerel liste üzerinde uygulanır. */
  protected readonly accountList = linkedSignal<readonly CustomerAccount[], CustomerAccount[]>({
    source: this.accounts,
    computation: (accounts) => [...accounts]
  });

  /** 'list' tablo görünümü, 'form' oluştur/düzenle formu. */
  protected readonly mode = signal<'list' | 'form'>('list');

  /** Formdaki hesap; `null` ise yeni hesap oluşturuluyor demektir. */
  protected readonly editingAccount = signal<CustomerAccount | null>(null);

  /** Silme onayı bekleyen hesap; `null` ise dialog kapalıdır. */
  protected readonly deletingAccount = signal<CustomerAccount | null>(null);

  /** Başka bir müşteriye geçildiğinde sayfa başa döner. */
  protected readonly page = linkedSignal<CustomerAccount[], number>({
    source: this.accountList,
    computation: () => 1
  });

  protected readonly accountPages = computed(() => {
    const pageCount = Math.ceil(this.accountList().length / PAGE_SIZE);
    return Array.from({ length: pageCount }, (_, index) => index + 1);
  });

  protected readonly visibleAccounts = computed(() => {
    const start = (this.page() - 1) * PAGE_SIZE;
    return this.accountList().slice(start, start + PAGE_SIZE);
  });

  /** Tasarımda olduğu gibi her sayfanın ilk hesabı açık gelir. */
  protected readonly expandedAccount = linkedSignal<readonly CustomerAccount[], string | null>({
    source: this.visibleAccounts,
    computation: (accounts) => accounts[0]?.number ?? null
  });

  protected toggleAccount(accountNumber: string): void {
    this.expandedAccount.update((current) => (current === accountNumber ? null : accountNumber));
  }

  protected openCreate(): void {
    this.editingAccount.set(null);
    this.mode.set('form');
  }

  protected openEdit(account: CustomerAccount): void {
    this.editingAccount.set(account);
    this.mode.set('form');
  }

  protected closeForm(): void {
    this.mode.set('list');
    this.editingAccount.set(null);
  }

  protected saveAccount(result: AccountFormResult): void {
    const editing = this.editingAccount();

    if (editing === null) {
      const account: CustomerAccount = {
        number: String(Date.now()),
        name: result.name,
        accountType: 'Billing Account',
        status: 'active',
        products: []
      };
      this.accountList.update((accounts) => [account, ...accounts]);
    } else {
      this.accountList.update((accounts) =>
        accounts.map((account) =>
          account.number === editing.number ? { ...account, name: result.name } : account
        )
      );
    }

    this.closeForm();
  }

  protected requestDelete(account: CustomerAccount): void {
    this.deletingAccount.set(account);
  }

  protected confirmDelete(): void {
    const deleting = this.deletingAccount();
    if (deleting === null) {
      return;
    }

    this.accountList.update((accounts) =>
      accounts.filter((account) => account.number !== deleting.number)
    );
    this.deletingAccount.set(null);

    const lastPage = Math.max(this.accountPages().length, 1);
    if (this.page() > lastPage) {
      this.page.set(lastPage);
    }
  }

  protected cancelDelete(): void {
    this.deletingAccount.set(null);
  }
}
