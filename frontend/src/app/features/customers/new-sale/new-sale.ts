import {
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  signal
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { ShellTitle } from '../../../layout/shell-title';
import { Breadcrumb, BreadcrumbItem } from '../../../shared/ui/breadcrumb/breadcrumb';
import { Button } from '../../../shared/ui/button/button';
import { Icon } from '../../../shared/ui/icon/icon';
import { CustomerAddress, CustomerOrder } from '../customer.model';
import { CustomerService } from '../customer.service';
import { CustomerAddressCard } from '../customer-detail/customer-address-card';
import {
  AddressFormResult,
  CustomerAddressForm
} from '../customer-detail/customer-address-form';
import { CartItem, ConfigField } from './sales.model';
import { CAMPAIGNS, PRODUCT_OFFERS, offerConfig } from './sales.mock';

type SaleStep = 'offer' | 'config' | 'submit' | 'done';
type OfferTab = 'catalog' | 'campaign';

/** "Yeni Satış Başlat" akışı: teklif seçimi → konfigürasyon → sipariş gönder → onay. */
@Component({
  selector: 'app-new-sale',
  imports: [DecimalPipe, Breadcrumb, Button, Icon, CustomerAddressCard, CustomerAddressForm],
  templateUrl: './new-sale.html'
})
export class NewSale {
  private readonly customers = inject(CustomerService);
  private readonly router = inject(Router);
  private readonly shellTitle = inject(ShellTitle);

  protected readonly t = inject(I18nService).t;

  /** `customers/:id/new-sale` route parametresi. */
  readonly id = input.required<string>();

  protected readonly offers = PRODUCT_OFFERS;
  protected readonly campaigns = CAMPAIGNS;

  protected readonly step = signal<SaleStep>('offer');
  protected readonly activeTab = signal<OfferTab>('catalog');

  // Katalog/kampanya arama alanları (taslak) ve "Ara" ile uygulanan değerler.
  protected readonly catalogGroup = signal('');
  protected readonly offerIdInput = signal('');
  protected readonly offerNameInput = signal('');
  private readonly appliedOfferId = signal('');
  private readonly appliedOfferName = signal('');

  protected readonly campaignGroup = signal('');
  protected readonly campaignIdInput = signal('');
  protected readonly campaignNameInput = signal('');
  private readonly appliedCampaignId = signal('');
  private readonly appliedCampaignName = signal('');

  protected readonly selectedOfferId = signal<string | null>(null);
  protected readonly selectedCampaignId = signal<string | null>(null);

  protected readonly cart = signal<CartItem[]>([]);
  protected readonly orderId = signal<string>('');

  private readonly customer = computed(() => this.customers.getById(this.id()));

  /** Konfigürasyon adımındaki hizmet adresi listesi; yeni adres eklendikçe büyür. */
  protected readonly addressList = linkedSignal<CustomerAddress[] | null, CustomerAddress[]>({
    source: () => this.customer()?.addresses.slice() ?? null,
    computation: (addresses) => addresses ?? []
  });

  /** Seçili hizmet adresi; varsayılan olarak birincil/ilk adres. */
  protected readonly selectedAddressId = linkedSignal<CustomerAddress[], string | null>({
    source: this.addressList,
    computation: (addresses, previous) => {
      const previousId = previous?.value ?? null;
      if (previousId !== null && addresses.some((address) => address.id === previousId)) {
        return previousId;
      }
      return addresses.find((address) => address.isPrimary)?.id ?? addresses[0]?.id ?? null;
    }
  });

  /** Konfigürasyon formu açık mı (yeni adres ekleme alt-modu). */
  protected readonly addingAddress = signal(false);

  /** Konfigürasyon alan değerleri; anahtar `${cartKey}::${fieldName}`. */
  private readonly configValues = signal<Record<string, string>>({});

  protected readonly serviceAddress = computed<CustomerAddress | null>(
    () => this.addressList().find((address) => address.id === this.selectedAddressId()) ?? null
  );

  /** Tüm zorunlu konfigürasyon alanları dolu ve bir hizmet adresi seçili mi? */
  protected readonly configValid = computed(() => {
    if (this.selectedAddressId() === null) {
      return false;
    }
    const values = this.configValues();
    return this.cart().every((item) =>
      offerConfig(item.offerId).every(
        (field) => (values[`${item.key}::${field.name}`] ?? '').trim() !== ''
      )
    );
  });

  protected readonly cartTotal = computed(() =>
    this.cart().reduce((sum, item) => sum + item.price, 0)
  );

  protected readonly filteredOffers = computed(() => {
    const byId = this.appliedOfferId().trim();
    const byName = this.appliedOfferName().trim().toLocaleLowerCase('tr-TR');
    return this.offers.filter(
      (offer) =>
        offer.id.includes(byId) && offer.name.toLocaleLowerCase('tr-TR').includes(byName)
    );
  });

  protected readonly filteredCampaigns = computed(() => {
    const byId = this.appliedCampaignId().trim();
    const byName = this.appliedCampaignName().trim().toLocaleLowerCase('tr-TR');
    return this.campaigns.filter(
      (campaign) =>
        campaign.id.includes(byId) &&
        campaign.name.toLocaleLowerCase('tr-TR').includes(byName)
    );
  });

  protected readonly stepTitle = computed(() => {
    const s = this.t().sales;
    if (this.step() === 'offer') {
      return s.offer.title;
    }
    return this.step() === 'config' ? s.config.title : s.submit.title;
  });

  protected readonly breadcrumb = computed<readonly BreadcrumbItem[]>(() => {
    const s = this.t().sales;
    const account: BreadcrumbItem = { label: s.breadcrumbAccount, link: `/customers/${this.id()}` };

    if (this.step() === 'offer') {
      return [account, { label: s.offer.title }];
    }
    if (this.step() === 'config') {
      return [account, { label: s.config.title }];
    }
    return [account, { label: s.config.title }, { label: s.submit.title }];
  });

  constructor() {
    // Üst bar başlığını adıma göre değiştir; akıştan çıkınca varsayılana dön.
    effect(() => {
      const s = this.t().sales;
      const title =
        this.step() === 'offer'
          ? s.offer.title
          : this.step() === 'config'
            ? s.config.title
            : s.submit.title;
      this.shellTitle.override.set({ title, subtitle: s.subtitle });
    });

    inject(DestroyRef).onDestroy(() => this.shellTitle.override.set(null));
  }

  /** Kampanya teklif id'lerini virgülle birleştirir (tablo gösterimi). */
  protected campaignOfferIds(campaignId: string): string {
    const campaign = this.campaigns.find((item) => item.id === campaignId);
    return campaign ? campaign.items.map((item) => item.offerId).join(', ') : '';
  }

  /** Kampanyadaki teklif adlarını virgülle birleştirir (tablo gösterimi). */
  protected campaignOfferNames(campaignId: string): string {
    const campaign = this.campaigns.find((item) => item.id === campaignId);
    if (campaign === undefined) {
      return '';
    }
    return campaign.items
      .map((item) => this.offers.find((offer) => offer.id === item.offerId)?.name ?? item.offerId)
      .join(', ');
  }

  /** Bir teklifin konfigürasyon alanlarını döndürür. */
  protected configFields(offerId: string): readonly ConfigField[] {
    return offerConfig(offerId);
  }

  protected configValue(cartKey: string, fieldName: string): string {
    return this.configValues()[`${cartKey}::${fieldName}`] ?? '';
  }

  protected setConfigValue(cartKey: string, fieldName: string, value: string): void {
    this.configValues.update((values) => ({ ...values, [`${cartKey}::${fieldName}`]: value }));
  }

  protected openAddAddress(): void {
    this.addingAddress.set(true);
  }

  protected cancelAddAddress(): void {
    this.addingAddress.set(false);
  }

  /** Yeni adresi listeye ekler, seçili yapar ve formu kapatır. */
  protected saveNewAddress(result: AddressFormResult): void {
    const address: CustomerAddress = {
      id: String(Date.now()),
      title: `${result.city}, ${result.street}, ${result.buildingNo}`,
      detail: result.description,
      isPrimary: false
    };
    this.addressList.update((addresses) => [...addresses, address]);
    this.selectedAddressId.set(address.id);
    this.addingAddress.set(false);
  }

  protected searchCatalog(): void {
    this.appliedOfferId.set(this.offerIdInput());
    this.appliedOfferName.set(this.offerNameInput());
  }

  protected searchCampaign(): void {
    this.appliedCampaignId.set(this.campaignIdInput());
    this.appliedCampaignName.set(this.campaignNameInput());
  }

  protected addToCart(): void {
    if (this.activeTab() === 'catalog') {
      const offerId = this.selectedOfferId();
      const offer = this.offers.find((item) => item.id === offerId);
      if (offer === undefined) {
        return;
      }
      this.pushCartItem({
        key: `cat:${offer.id}`,
        offerId: offer.id,
        name: offer.name,
        campaignId: null,
        price: offer.price,
        basePrice: offer.price
      });
      return;
    }

    const campaign = this.campaigns.find((item) => item.id === this.selectedCampaignId());
    if (campaign === undefined) {
      return;
    }
    for (const item of campaign.items) {
      const offer = this.offers.find((candidate) => candidate.id === item.offerId);
      this.pushCartItem({
        key: `${campaign.id}:${item.offerId}`,
        offerId: item.offerId,
        name: offer?.name ?? item.offerId,
        campaignId: campaign.id,
        price: item.price,
        basePrice: offer?.price ?? item.price
      });
    }
  }

  /** Aynı satır iki kez eklenmesin diye anahtar kontrolüyle ekler. */
  private pushCartItem(item: CartItem): void {
    this.cart.update((cart) =>
      cart.some((existing) => existing.key === item.key) ? cart : [...cart, item]
    );
  }

  protected clearCart(): void {
    this.cart.set([]);
  }

  protected goToOffer(): void {
    this.step.set('offer');
  }

  protected goToConfig(): void {
    this.step.set('config');
  }

  protected goToSubmit(): void {
    if (this.orderId() === '') {
      this.orderId.set(String(Date.now()).slice(-8));
    }
    this.step.set('submit');
  }

  protected send(): void {
    const items = this.cart();
    const first = items[0];
    const order: CustomerOrder = {
      orderNumber: this.orderId(),
      date: new Date().toISOString().slice(0, 10),
      product:
        first === undefined
          ? ''
          : items.length > 1
            ? `${first.name} +${items.length - 1}`
            : first.name,
      amount: this.cartTotal(),
      status: 'pending'
    };

    this.customers.addOrder(Number(this.id()), order);
    this.step.set('done');
  }

  protected backToSearch(): void {
    void this.router.navigate(['/customers']);
  }
}
