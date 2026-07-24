export const CUSTOMER_TYPES = ['B2C', 'B2B'] as const;

export type CustomerType = (typeof CUSTOMER_TYPES)[number];

export type CustomerStatus = 'active' | 'passive';

export type Gender = 'male' | 'female';

export type OrderStatus = 'completed' | 'pending' | 'cancelled';

export interface CustomerOrder {
  readonly orderNumber: string;
  readonly date: string;
  readonly product: string;
  readonly amount: number;
  readonly status: OrderStatus;
}

/** Ürün önizlemesinde gösterilen hizmet adresi; adresi olmayan ürünlerde null'dır. */
export interface CustomerProductAddress {
  /** Adresin kısa adı: "Home". */
  readonly name: string;
  /** Adresin başlığı: "İstanbul, Bağdat Cd.". */
  readonly title: string;
  readonly buildingNo: string;
  readonly description: string;
}

/** Ürün önizleme penceresinde gösterilen teklif/spesifikasyon detayları. */
export interface CustomerProductPreview {
  readonly offerId: string;
  readonly offerName: string;
  readonly specId: string;
  /** Ürün özelliklerinin serbest metin özeti. */
  readonly characteristics: string;
  readonly address: CustomerProductAddress | null;
}

/** Hesap altındaki ürün/aksiyon satırı; kampanya alanları boş olabilir. */
export interface CustomerAccountProduct {
  readonly id: string;
  readonly name: string;
  readonly campaignName: string | null;
  readonly campaignId: string | null;
  /** Göz ikonuyla açılan önizleme verisi; yoksa null. */
  readonly preview: CustomerProductPreview | null;
}

export interface CustomerAccount {
  readonly number: string;
  readonly name: string;
  readonly accountType: string;
  readonly status: CustomerStatus;
  readonly products: readonly CustomerAccountProduct[];
  /**
   * Backend'den yüklenen alanlar (BFF detay yanıtı). Bellek içi mock kayıtlarda
   * bulunmaz; güncelleme yalnızca backend'den gelen hesaplarda yapılır.
   */
  readonly id?: number;
  readonly accountNumber?: string | null;
  readonly orderNumber?: string | null;
  readonly addressId?: number | null;
  readonly accountDescription?: string | null;
  /** Hesaba bağlı aktif ürün sayısı; müşteri silme kuralında kullanılır (aktif ürünlü silinemez). */
  readonly activeProductCount?: number;
}

export interface CustomerAddress {
  readonly id: string;
  /** Kartın başlığı: "İstanbul, Atatürk Cd., 14/7". */
  readonly title: string;
  readonly detail: string;
  readonly isPrimary: boolean;
  /**
   * Backend'in ayrı sakladığı yapısal alanlar. Oluşturma/düzenleme akışlarında doldurulur;
   * salt görüntülemede (title/detail yeterliyken) opsiyoneldir.
   */
  readonly city?: string;
  readonly street?: string;
  readonly houseNumber?: string;
}

/** İletişim kanalı alanları; müşteride tanımlı değilse null. */
export interface CustomerContact {
  /**
   * Backend iletişim bilgisi id'si; BFF detay yanıtından gelir. Bellek içi mock
   * kayıtlarda bulunmaz, kayıt yoksa null olur (güncelleme yapılamaz).
   */
  readonly id?: number | null;
  readonly email: string | null;
  readonly mobilePhone: string | null;
  readonly homePhone: string | null;
  readonly fax: string | null;
}

export interface Customer {
  readonly id: number;
  /** Ekranda gösterilen müşteri kodu: "C-1100014". */
  readonly code: string;
  readonly type: CustomerType;
  /** B2C müşterilerde ad, B2B müşterilerde yetkili kişinin adı. */
  readonly firstName: string;
  readonly secondName: string | null;
  readonly lastName: string;
  /** Yalnızca B2B müşterilerde dolu. */
  readonly companyName: string | null;
  /** B2C için TC kimlik no, B2B için vergi kimlik no. */
  readonly identityNumber: string;
  readonly gender: Gender;
  readonly birthDate: string;
  readonly motherName: string;
  readonly fatherName: string;
  readonly accountNumber: string;
  readonly gsm: string;
  readonly city: string;
  readonly status: CustomerStatus;
  readonly registeredAt: string;
  readonly contact: CustomerContact;
  readonly addresses: readonly CustomerAddress[];
  readonly accounts: readonly CustomerAccount[];
  readonly orders: readonly CustomerOrder[];
}

/** Listede ve detay başlığında gösterilecek ad. */
export function customerDisplayName(customer: Customer): string {
  if (customer.companyName !== null) {
    return customer.companyName;
  }

  return [customer.firstName, customer.secondName, customer.lastName]
    .filter((part) => part !== null && part !== '')
    .join(' ');
}

/** Avatar dairesindeki baş harfler: ad + soyad. */
export function customerInitials(customer: Customer): string {
  const parts =
    customer.companyName !== null
      ? customer.companyName.split(' ')
      : [customer.firstName, customer.lastName];

  return parts
    .filter((part) => part.length > 0)
    .slice(0, 2)
    .map((part) => part.charAt(0).toLocaleUpperCase('tr-TR'))
    .join('');
}
