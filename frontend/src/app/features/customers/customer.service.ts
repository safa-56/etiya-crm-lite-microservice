import { Service } from '@angular/core';

import { Customer, CustomerOrder, CustomerType } from './customer.model';
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

  /**
   * Yeni müşteriyi bellek içi listeye ekler ve oluşturulan kaydı döndürür. Gerçek uçlara
   * geçilirken burası POST çağrısına dönüşür.
   */
  create(customer: Customer): Customer {
    (MOCK_CUSTOMERS as Customer[]).unshift(customer);
    return customer;
  }

  /** Route parametresi string geldiği için karşılaştırma string üzerinden yapılır. */
  getById(id: string): Customer | null {
    return MOCK_CUSTOMERS.find((candidate) => String(candidate.id) === id) ?? null;
  }

  /**
   * Müşterinin verilen alanlarını günceller. Şu an bellek içi mock kaydı yerinde değiştirir;
   * gerçek uçlara geçilirken burası PUT/PATCH çağrısına dönüşür.
   */
  update(id: number, changes: Partial<Customer>): Customer | null {
    const target = MOCK_CUSTOMERS.find((candidate) => candidate.id === id);
    if (target === undefined) {
      return null;
    }

    Object.assign(target, changes);
    return target;
  }

  /**
   * Müşteriyi bellek içi listeden çıkarır. Gerçek uçlara geçilirken burası DELETE çağrısına
   * dönüşür.
   */
  remove(id: number): void {
    const index = MOCK_CUSTOMERS.findIndex((candidate) => candidate.id === id);
    if (index !== -1) {
      (MOCK_CUSTOMERS as Customer[]).splice(index, 1);
    }
  }

  /** Yeni satış akışı tamamlandığında müşterinin sipariş listesine ekler. */
  addOrder(customerId: number, order: CustomerOrder): void {
    const target = MOCK_CUSTOMERS.find((candidate) => candidate.id === customerId);
    if (target !== undefined) {
      (target.orders as CustomerOrder[]).unshift(order);
    }
  }
}
