/** Katalogdaki tek ürün teklifi. */
export interface ProductOffer {
  readonly id: string;
  readonly name: string;
  /** Katalogdan tekil eklendiğindeki liste fiyatı. */
  readonly price: number;
}

/** Bir kampanyanın içerdiği teklif ve o kampanyadaki indirimli fiyatı. */
export interface CampaignItem {
  readonly offerId: string;
  readonly price: number;
}

/** Birden çok teklifi indirimli fiyatlarla paketleyen kampanya. */
export interface Campaign {
  readonly id: string;
  readonly name: string;
  readonly items: readonly CampaignItem[];
}

/** Ürün konfigürasyon adımında bir teklif için doldurulacak alan. */
export interface ConfigField {
  /** Teklif içinde benzersiz alan anahtarı. */
  readonly name: string;
  /** Ekranda gösterilen (dilden bağımsız teknik) etiket. */
  readonly label: string;
}

/** Sepete eklenen satır. */
export interface CartItem {
  /** Aynı teklif farklı kampanyalarla eklenebildiği için benzersiz satır anahtarı. */
  readonly key: string;
  readonly offerId: string;
  readonly name: string;
  readonly campaignId: string | null;
  /** Uygulanan fiyat. */
  readonly price: number;
  /** Katalog liste fiyatı; `price`'tan yüksekse üstü çizili gösterilir. */
  readonly basePrice: number;
}
