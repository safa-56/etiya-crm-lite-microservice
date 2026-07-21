import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { DatePipe, formatDate } from '@angular/common';
import { RouterLink } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import {
  Customer,
  CustomerAccount,
  customerDisplayName,
  customerInitials
} from '../customer.model';
import { MOCK_CUSTOMERS } from '../customers.mock';

/** Hesap tablosunda bir sayfada gösterilecek hesap sayısı. */
const ACCOUNTS_PAGE_SIZE = 4;

type DetailTab = 'info' | 'account' | 'address' | 'contact';

@Component({
  selector: 'app-customer-detail',
  imports: [DatePipe, RouterLink],
  templateUrl: './customer-detail.html'
})
export class CustomerDetail {
  private readonly i18n = inject(I18nService);

  /** `customers/:id` route parametresi; component input binding ile bağlanır. */
  readonly id = input.required<string>();

  protected readonly t = this.i18n.t;

  protected readonly customer = computed<Customer | null>(
    () => MOCK_CUSTOMERS.find((candidate) => String(candidate.id) === this.id()) ?? null
  );

  /** Başka bir müşteriye geçildiğinde sekme ve sayfa durumu başa döner. */
  protected readonly activeTab = linkedSignal<Customer | null, DetailTab>({
    source: this.customer,
    computation: () => 'info'
  });

  protected readonly accountsPage = linkedSignal<Customer | null, number>({
    source: this.customer,
    computation: () => 1
  });

  private readonly accounts = computed<readonly CustomerAccount[]>(
    () => this.customer()?.accounts ?? []
  );

  protected readonly accountPages = computed(() => {
    const pageCount = Math.ceil(this.accounts().length / ACCOUNTS_PAGE_SIZE);
    return Array.from({ length: pageCount }, (_, index) => index + 1);
  });

  protected readonly visibleAccounts = computed(() => {
    const start = (this.accountsPage() - 1) * ACCOUNTS_PAGE_SIZE;
    return this.accounts().slice(start, start + ACCOUNTS_PAGE_SIZE);
  });

  /** Tasarımda olduğu gibi her sayfanın ilk hesabı açık gelir. */
  protected readonly expandedAccount = linkedSignal<readonly CustomerAccount[], string | null>({
    source: this.visibleAccounts,
    computation: (accounts) => accounts[0]?.number ?? null
  });

  protected readonly primaryAddressId = linkedSignal<Customer | null, string | null>({
    source: this.customer,
    computation: (customer) => customer?.addresses.find((address) => address.isPrimary)?.id ?? null
  });

  /** "Temmuz 2021'den beri" gibi üyelik metni; ay adı aktif dile göre biçimlenir. */
  protected readonly memberSince = computed(() => {
    const customer = this.customer();

    if (customer === null) {
      return '';
    }

    const monthYear = formatDate(customer.registeredAt, 'LLLL yyyy', this.i18n.language());
    return this.t().customers.detail.memberSince.replace('{date}', monthYear);
  });

  protected readonly displayName = customerDisplayName;
  protected readonly initials = customerInitials;

  protected toggleAccount(accountNumber: string): void {
    this.expandedAccount.update((current) => (current === accountNumber ? null : accountNumber));
  }
}
