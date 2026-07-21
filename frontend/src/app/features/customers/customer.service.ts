import { Service } from '@angular/core';

import { Customer, CustomerType } from './customer.model';
import { MOCK_CUSTOMERS } from './customers.mock';

/** Arama panelinden gelen filtre alanları; boş string "bu alanı dikkate alma" demektir. */
export interface CustomerSearchFilters {
  readonly identityNumber: string;
  readonly customerId: string;
  readonly accountNumber: string;
  readonly gsm: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly companyName: string;
  readonly orderNumber: string;
}

/** Boş filtre alanları aramayı daraltmaz. */
function matchesContains(source: string, term: string): boolean {
  const needle = term.trim();
  return needle === '' || source.includes(needle);
}

function matchesStartsWith(source: string, term: string): boolean {
  const needle = term.trim().toLocaleLowerCase('tr-TR');
  return needle === '' || source.toLocaleLowerCase('tr-TR').startsWith(needle);
}

function hasMatchingOrder(customer: Customer, orderNumber: string): boolean {
  const needle = orderNumber.trim();
  return needle === '' || customer.orders.some((order) => order.orderNumber.includes(needle));
}

/**
 * Müşteri verisine tek giriş noktası. Şu an bellek içi mock veriyi okur; gerçek uçlara
 * geçilirken yalnızca bu sınıf değişir, bileşenler aynı kalır.
 */
@Service()
export class CustomerService {
  search(type: CustomerType, filters: CustomerSearchFilters): readonly Customer[] {
    return MOCK_CUSTOMERS.filter(
      (customer) =>
        customer.type === type &&
        matchesContains(customer.identityNumber, filters.identityNumber) &&
        matchesContains(String(customer.id), filters.customerId) &&
        matchesContains(customer.accountNumber, filters.accountNumber) &&
        matchesContains(customer.gsm, filters.gsm) &&
        matchesStartsWith(customer.firstName, type === 'B2C' ? filters.firstName : '') &&
        matchesStartsWith(customer.lastName, type === 'B2C' ? filters.lastName : '') &&
        matchesStartsWith(customer.companyName ?? '', type === 'B2B' ? filters.companyName : '') &&
        hasMatchingOrder(customer, filters.orderNumber)
    );
  }

  /** Route parametresi string geldiği için karşılaştırma string üzerinden yapılır. */
  getById(id: string): Customer | null {
    return MOCK_CUSTOMERS.find((candidate) => String(candidate.id) === id) ?? null;
  }
}
