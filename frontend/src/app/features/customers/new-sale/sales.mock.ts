import { Campaign, ConfigField, ProductOffer } from './sales.model';

/** Katalog ürün teklifleri; tasarım aşaması için örnek veri. */
export const PRODUCT_OFFERS: readonly ProductOffer[] = [
  { id: '71233', name: "8 Mbps'ye kadar / 4 GB kotalı ADSL Internet", price: 59.9 },
  { id: '202610', name: 'Müşteri Modemi PR', price: 79.9 },
  { id: '126030', name: 'Aktivasyon / Peşin', price: 79.9 },
  { id: '305011', name: 'Fiber 100 Mbps / Sınırsız', price: 149.9 },
  { id: '410222', name: 'IPTV Premium Paket', price: 89.9 },
  { id: '588190', name: 'Statik IP Hizmeti', price: 49.9 },
  { id: '340150', name: 'Mobil 20GB Paylaşımlı Paket', price: 69.9 }
];

/** Kampanyalar; her biri katalogdaki tekliflerden bir alt kümeyi indirimli sunar. */
export const CAMPAIGNS: readonly Campaign[] = [
  {
    id: '1001',
    name: 'ADSL Başlangıç Paketi',
    items: [
      { offerId: '71233', price: 39.9 },
      { offerId: '202610', price: 0 }
    ]
  },
  {
    id: '1044',
    name: 'Fiber Eğlence Paketi',
    items: [
      { offerId: '305011', price: 119.9 },
      { offerId: '410222', price: 59.9 }
    ]
  },
  {
    id: '2210',
    name: 'Modem + Aktivasyon',
    items: [
      { offerId: '202610', price: 49.9 },
      { offerId: '126030', price: 59.9 }
    ]
  },
  {
    id: '3320',
    name: 'IPTV + Statik IP',
    items: [
      { offerId: '410222', price: 69.9 },
      { offerId: '588190', price: 29.9 }
    ]
  },
  {
    id: '4102',
    name: 'Statik IP + Mobil',
    items: [
      { offerId: '588190', price: 39.9 },
      { offerId: '340150', price: 49.9 }
    ]
  }
];

/**
 * Teklif başına ürün konfigürasyon alanları. Boş liste, o teklifin ek konfigürasyon
 * gerektirmediği anlamına gelir.
 */
export const OFFER_CONFIGS: Record<string, readonly ConfigField[]> = {
  '71233': [
    { name: 'portNo', label: 'Port No' },
    { name: 'circuitNo', label: 'Devre No' }
  ],
  '202610': [
    { name: 'modemBrand', label: 'Modem Brand' },
    { name: 'modemModel', label: 'Modem Model' },
    { name: 'modemSn', label: 'Modem SN' }
  ],
  '126030': [],
  '305011': [
    { name: 'fiberNo', label: 'Fiber No' },
    { name: 'ontSerial', label: 'ONT Serial' },
    { name: 'bandwidth', label: 'Bandwidth' }
  ],
  '410222': [
    { name: 'stbSerial', label: 'STB Serial' },
    { name: 'smartCardNo', label: 'Smart Card No' }
  ],
  '588190': [
    { name: 'ipAddress', label: 'IP Address' },
    { name: 'subnet', label: 'Subnet' }
  ],
  '340150': [
    { name: 'msisdn', label: 'MSISDN' },
    { name: 'simNo', label: 'SIM No' }
  ]
};

/** Verilen teklifin konfigürasyon alanlarını döndürür. */
export function offerConfig(offerId: string): readonly ConfigField[] {
  return OFFER_CONFIGS[offerId] ?? [];
}

/** Teklif adını id üzerinden çözer. */
export function offerName(offerId: string): string {
  return PRODUCT_OFFERS.find((offer) => offer.id === offerId)?.name ?? offerId;
}
