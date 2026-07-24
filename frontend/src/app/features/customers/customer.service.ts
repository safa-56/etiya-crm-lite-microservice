import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { API_BASE_URL } from '../../core/config/api.config';
import {
  Customer,
  CustomerAccount,
  CustomerAddress,
  CustomerContact,
  CustomerOrder,
  CustomerStatus,
  CustomerType,
  Gender
} from './customer.model';
import { MOCK_CUSTOMERS } from './customers.mock';

/**
 * BFF müşteri detay yanıtının şekli (bff-service `CustomerDetailResponse`).
 * Alanlar backend DTO'larıyla birebir; frontend görünüm modeline burada eşlenir.
 */
interface BffContactInfo {
  readonly email: string | null;
  readonly homePhone: string | null;
  readonly mobilePhone: string | null;
  readonly fax: string | null;
}

interface BffAddress {
  readonly id: number;
  readonly city: string;
  readonly street: string;
  readonly houseNumber: string;
  readonly addressDescription: string;
  readonly isPrimary: boolean;
}

interface BffIndividualCustomer {
  readonly id: number;
  readonly firstName: string;
  readonly secondName: string | null;
  readonly lastName: string;
  readonly birthDate: string;
  readonly fatherName: string;
  readonly motherName: string;
  readonly nationalityId: string;
  readonly genderType: string;
  readonly status: string;
  readonly createdDate: string;
  readonly contactInfos: readonly BffContactInfo[];
  readonly addresses: readonly BffAddress[];
}

interface BffBillingAccount {
  readonly id: number;
  readonly accountNumber: string | null;
  readonly accountName: string;
  readonly accountType: string | null;
  readonly status: string;
}

interface BffCustomerDetail {
  readonly customer: BffIndividualCustomer;
  readonly accounts: readonly BffBillingAccount[];
}

/** customer-service iç içe create gövdeleri (customerId gönderilmez; backend bağlar). */
export interface CreateContactInfoBody {
  readonly email: string;
  readonly homePhone: string | null;
  readonly mobilePhone: string;
  readonly fax: string | null;
}

export interface CreateAddressBody {
  readonly city: string;
  readonly street: string;
  readonly houseNumber: string;
  readonly addressDescription: string;
  readonly isPrimary: boolean;
}

export interface CreateIndividualCustomerBody {
  readonly firstName: string;
  readonly secondName: string | null;
  readonly lastName: string;
  readonly birthDate: string;
  readonly fatherName: string | null;
  readonly motherName: string | null;
  readonly nationalityId: string;
  readonly genderType: 'MALE' | 'FEMALE';
  readonly contactInfo: CreateContactInfoBody;
  readonly address: CreateAddressBody;
}

/** Backend cinsiyet enum'unu görünüm modeline indirger. */
function toGender(genderType: string): Gender {
  return genderType?.toUpperCase() === 'FEMALE' ? 'female' : 'male';
}

/**
 * Backend durum metnini ('ACTIVE', ref. kısa kod vb.) görünüm durumuna eşler.
 * Pasif/silinmiş işaretleri dışında varsayılan 'active' kabul edilir.
 */
function toStatus(status: string): CustomerStatus {
  const normalized = (status ?? '').toLowerCase();
  const passive =
    normalized.includes('pass') || normalized.includes('pasif') || normalized.includes('del');
  return passive ? 'passive' : 'active';
}

/** BFF adresini kartın beklediği tek satır başlık + açıklama biçimine çevirir. */
function toAddress(address: BffAddress): CustomerAddress {
  return {
    id: String(address.id),
    title: [address.city, address.street, address.houseNumber].filter((part) => part).join(', '),
    detail: address.addressDescription,
    isPrimary: address.isPrimary
  };
}

/** BFF fatura hesabını görünüm modeline eşler (ürünler product-service kapsamında; şimdilik boş). */
function toAccount(account: BffBillingAccount): CustomerAccount {
  return {
    number: account.accountNumber ?? String(account.id),
    name: account.accountName,
    accountType: account.accountType ?? '',
    status: toStatus(account.status),
    products: []
  };
}

/** BFF detay yanıtını frontend `Customer` görünüm modeline eşler. */
function toCustomer(detail: BffCustomerDetail): Customer {
  const c = detail.customer;
  const primaryContact = c.contactInfos[0] ?? null;
  const addresses = c.addresses.map(toAddress);
  const primaryAddress = c.addresses.find((address) => address.isPrimary) ?? c.addresses[0] ?? null;
  const accounts = detail.accounts.map(toAccount);

  const contact: CustomerContact = {
    email: primaryContact?.email ?? null,
    mobilePhone: primaryContact?.mobilePhone ?? null,
    homePhone: primaryContact?.homePhone ?? null,
    fax: primaryContact?.fax ?? null
  };

  return {
    id: c.id,
    // Ayrı bir müşteri kodu backend'de yok; gösterim id üzerinden yapılır.
    code: String(c.id),
    // customer-service yalnızca bireysel (B2C) müşteri tutar.
    type: 'B2C' as CustomerType,
    firstName: c.firstName,
    secondName: c.secondName,
    lastName: c.lastName,
    companyName: null,
    identityNumber: c.nationalityId,
    gender: toGender(c.genderType),
    birthDate: c.birthDate,
    motherName: c.motherName,
    fatherName: c.fatherName,
    accountNumber: accounts[0]?.number ?? '',
    gsm: contact.mobilePhone ?? '',
    city: primaryAddress?.city ?? '',
    status: toStatus(c.status),
    registeredAt: c.createdDate,
    contact,
    addresses,
    accounts,
    // Siparişler detay ekranında kullanılmıyor (order-service kapsam dışı).
    orders: []
  };
}

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

/** search-service `CustomerSearchResponse` satırı (yalnızca sonuç kolonları). */
interface SearchCustomerRow {
  readonly customerId: number;
  readonly firstName: string;
  readonly secondName: string | null;
  readonly lastName: string;
  readonly role: string;
  readonly nationalityId: string;
}

/** search-service `PagedResponse<T>` zarfı: içerik + sayfalama üstverisi. */
interface SearchPagedResponse<T> {
  readonly content: readonly T[];
  readonly pageNumber: number;
  readonly pageSize: number;
  readonly totalElements: number;
  readonly totalPages: number;
  readonly last: boolean;
}

/** Arama sonuç tablosunun satır görünüm modeli; backend kolonlarıyla birebir. */
export interface CustomerSearchRow {
  readonly customerId: number;
  readonly firstName: string;
  readonly secondName: string | null;
  readonly lastName: string;
  readonly role: CustomerType;
  readonly nationalityId: string;
}

/** Sayfalı arama sonucu görünüm modeli. */
export interface CustomerSearchResult {
  readonly rows: readonly CustomerSearchRow[];
  /** 0 tabanlı geçerli sayfa indeksi. */
  readonly pageNumber: number;
  readonly pageSize: number;
  readonly totalElements: number;
  readonly totalPages: number;
  readonly last: boolean;
}

/** Backend rol enum'unu ('B2C'/'B2B') görünüm tipine indirger. */
function toSearchRow(row: SearchCustomerRow): CustomerSearchRow {
  return {
    customerId: row.customerId,
    firstName: row.firstName,
    secondName: row.secondName,
    lastName: row.lastName,
    role: row.role?.toUpperCase() === 'B2B' ? 'B2B' : 'B2C',
    nationalityId: row.nationalityId
  };
}

/**
 * Müşteri verisine tek giriş noktası. Şu an bellek içi mock veriyi okur; gerçek uçlara
 * geçilirken yalnızca bu sınıf değişir, bileşenler aynı kalır.
 */
@Service()
export class CustomerService {
  private readonly http = inject(HttpClient);

  /** BFF müşteri detay ucu (gateway üzerinden): kimlik + iletişim + adres + hesaplar tek çağrıda. */
  private readonly detailUrl = `${API_BASE_URL}/bff-service/api/v1/customer-detail`;

  /** Bireysel müşteri CRUD ucu (gateway → customer-service; aggregation gerekmez). */
  private readonly individualCustomersUrl = `${API_BASE_URL}/customer-service/api/v1/individual-customers`;

  /** Müşteri arama ucu (gateway → search-service CQRS read-model'i). Sayfalıdır. */
  private readonly searchUrl = `${API_BASE_URL}/search-service/api/v1/search/customers`;

  /**
   * Yeni bireysel müşteriyi customer-service'e yazar (iç içe iletişim + adres, tek POST).
   * Gerçek DB'ye kayıt yapar; oluşan müşterinin id'sini döner. Hatalar çağırana propagate edilir.
   */
  createIndividual(body: CreateIndividualCustomerBody): Observable<number> {
    return this.http
      .post<{ id: number }>(this.individualCustomersUrl, body)
      .pipe(map((response) => response.id));
  }

  /**
   * Müşteri detayını BFF'ten çeker ve görünüm modeline eşler. Hatalar (404 dahil)
   * çağırana propagate edilir; bileşen yükleme/hata/bulunamadı durumlarını yönetir.
   */
  getDetailById(id: string): Observable<Customer> {
    return this.http
      .get<BffCustomerDetail>(`${this.detailUrl}/${id}`)
      .pipe(map((detail) => toCustomer(detail)));
  }

  /**
   * Müşterileri search-service'te (gerçek uç) sayfalı olarak arar. Boş filtre alanları
   * sorguya eklenmez (aramayı daraltmaz). Backend format ihlallerinde (400) ve diğer
   * hatalarda observable hata yayar; bileşen yükleme/hata/boş durumlarını yönetir.
   *
   * <p>Not: search read-model'i yalnızca bireysel (B2C) müşteri taşır; `companyName`
   * backend'de arama kriteri değildir ve gönderilmez.
   */
  search(
    type: CustomerType,
    filters: CustomerSearchFilters,
    page: number,
    size: number
  ): Observable<CustomerSearchResult> {
    let params = new HttpParams()
      .set('segment', type)
      .set('page', page)
      .set('size', size);

    // Boş olmayan kriterler trim'lenip sorguya eklenir; boşlar aramayı daraltmaz.
    const criteria: Record<string, string> = {
      idNumber: filters.identityNumber,
      customerId: filters.customerId,
      accountNumber: filters.accountNumber,
      gsm: filters.gsm,
      firstName: filters.firstName,
      lastName: filters.lastName,
      orderNumber: filters.orderNumber
    };
    for (const [key, value] of Object.entries(criteria)) {
      const trimmed = value.trim();
      if (trimmed !== '') {
        params = params.set(key, trimmed);
      }
    }

    return this.http
      .get<SearchPagedResponse<SearchCustomerRow>>(this.searchUrl, { params })
      .pipe(
        map((response) => ({
          rows: response.content.map(toSearchRow),
          pageNumber: response.pageNumber,
          pageSize: response.pageSize,
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          last: response.last
        }))
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
