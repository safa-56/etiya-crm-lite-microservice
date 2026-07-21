import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { I18nService } from '../../core/i18n/i18n.service';
import { Customer, CustomerType, customerDisplayName, customerInitials } from './customer.model';
import { MOCK_CUSTOMERS } from './customers.mock';

/** Boş filtre alanları aramayı daraltmaz. */
function matchesContains(source: string, term: string): boolean {
  const needle = term.trim();
  return needle === '' || source.includes(needle);
}

function matchesStartsWith(source: string, term: string): boolean {
  const needle = term.trim().toLocaleLowerCase('tr-TR');
  return needle === '' || source.toLocaleLowerCase('tr-TR').startsWith(needle);
}

@Component({
  selector: 'app-customers',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './customers.html'
})
export class Customers {
  private readonly formBuilder = inject(FormBuilder);

  protected readonly t = inject(I18nService).t;

  protected readonly form = this.formBuilder.nonNullable.group({
    identityNumber: '',
    customerId: '',
    accountNumber: '',
    gsm: '',
    firstName: '',
    lastName: '',
    companyName: '',
    orderNumber: ''
  });

  protected readonly customerType = signal<CustomerType>('B2C');

  /** Arama yapılmadan önce sonuç paneli yönlendirme metni gösterir. */
  protected readonly searched = signal(false);
  protected readonly results = signal<readonly Customer[]>([]);

  protected readonly isCorporate = computed(() => this.customerType() === 'B2B');

  protected readonly displayName = customerDisplayName;
  protected readonly initials = customerInitials;

  protected setCustomerType(type: CustomerType): void {
    if (this.customerType() === type) {
      return;
    }

    this.customerType.set(type);
    this.clear();
  }

  protected search(): void {
    const filters = this.form.getRawValue();
    const type = this.customerType();

    const matches = MOCK_CUSTOMERS.filter(
      (customer) =>
        customer.type === type &&
        matchesContains(customer.identityNumber, filters.identityNumber) &&
        matchesContains(String(customer.id), filters.customerId) &&
        matchesContains(customer.accountNumber, filters.accountNumber) &&
        matchesContains(customer.gsm, filters.gsm) &&
        matchesStartsWith(customer.firstName, type === 'B2C' ? filters.firstName : '') &&
        matchesStartsWith(customer.lastName, type === 'B2C' ? filters.lastName : '') &&
        matchesStartsWith(customer.companyName ?? '', type === 'B2B' ? filters.companyName : '') &&
        this.hasMatchingOrder(customer, filters.orderNumber)
    );

    this.results.set(matches);
    this.searched.set(true);
  }

  protected clear(): void {
    this.form.reset();
    this.results.set([]);
    this.searched.set(false);
  }

  private hasMatchingOrder(customer: Customer, orderNumber: string): boolean {
    const needle = orderNumber.trim();

    return needle === '' || customer.orders.some((order) => order.orderNumber.includes(needle));
  }
}
