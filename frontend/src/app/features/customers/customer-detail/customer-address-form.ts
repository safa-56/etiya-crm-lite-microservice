import { Component, computed, inject, input, linkedSignal, output } from '@angular/core';
import { FormField, form, required, schema } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerAddress } from '../customer.model';

/** Şehir açılır listesi için seçenekler: Türkiye'nin 81 ili, alfabetik sırayla. */
export const CITIES: readonly string[] = [
  'Adana',
  'Adıyaman',
  'Afyonkarahisar',
  'Ağrı',
  'Aksaray',
  'Amasya',
  'Ankara',
  'Antalya',
  'Ardahan',
  'Artvin',
  'Aydın',
  'Balıkesir',
  'Bartın',
  'Batman',
  'Bayburt',
  'Bilecik',
  'Bingöl',
  'Bitlis',
  'Bolu',
  'Burdur',
  'Bursa',
  'Çanakkale',
  'Çankırı',
  'Çorum',
  'Denizli',
  'Diyarbakır',
  'Düzce',
  'Edirne',
  'Elazığ',
  'Erzincan',
  'Erzurum',
  'Eskişehir',
  'Gaziantep',
  'Giresun',
  'Gümüşhane',
  'Hakkâri',
  'Hatay',
  'Iğdır',
  'Isparta',
  'İstanbul',
  'İzmir',
  'Kahramanmaraş',
  'Karabük',
  'Karaman',
  'Kars',
  'Kastamonu',
  'Kayseri',
  'Kırıkkale',
  'Kırklareli',
  'Kırşehir',
  'Kilis',
  'Kocaeli',
  'Konya',
  'Kütahya',
  'Malatya',
  'Manisa',
  'Mardin',
  'Mersin',
  'Muğla',
  'Muş',
  'Nevşehir',
  'Niğde',
  'Ordu',
  'Osmaniye',
  'Rize',
  'Sakarya',
  'Samsun',
  'Siirt',
  'Sinop',
  'Sivas',
  'Şanlıurfa',
  'Şırnak',
  'Tekirdağ',
  'Tokat',
  'Trabzon',
  'Tunceli',
  'Uşak',
  'Van',
  'Yalova',
  'Yozgat',
  'Zonguldak'
];

/** Formun ürettiği sonuç; adres panelinde `CustomerAddress`'e dönüştürülür. */
export interface AddressFormResult {
  city: string;
  street: string;
  buildingNo: string;
  description: string;
}

interface AddressDraft {
  city: string;
  street: string;
  buildingNo: string;
  description: string;
}

const addressDraftSchema = schema<AddressDraft>((draft) => {
  required(draft.city);
  required(draft.street);
  required(draft.buildingNo);
  required(draft.description);
});

/** `"İstanbul, Atatürk Cd., 14/7"` başlığını şehir/cadde/bina alanlarına ayırır. */
function toDraft(address: CustomerAddress | null): AddressDraft {
  if (address === null) {
    return { city: '', street: '', buildingNo: '', description: '' };
  }

  const [city = '', street = '', buildingNo = ''] = address.title.split(', ');
  return { city, street, buildingNo, description: address.detail };
}

/**
 * Adres ekleme/düzenleme formu. `address` verildiğinde alanlar o adresten dolar ve başlık
 * "Adres Düzenle" olur; verilmediğinde boş "Yeni Adres Ekle" formu açılır.
 */
@Component({
  selector: 'app-customer-address-form',
  imports: [FormField, Button, FormFieldShell, Icon, PanelHeader],
  host: { class: 'block' },
  template: `
    <app-panel-header [heading]="heading()" />

    <form (submit)="submit($event)">
      <div class="mt-6 grid gap-x-8 gap-y-5 sm:grid-cols-2">
        <app-form-field
          for="address-city"
          [label]="t().customers.detail.addresses.city"
          [required]="true"
        >
          <select
            id="address-city"
            class="field-control"
            [formField]="addressForm.city"
            aria-required="true"
          >
            <option value="" disabled>{{ t().customers.detail.addresses.cityPlaceholder }}</option>
            @for (city of cityOptions(); track city) {
              <option [value]="city">{{ city }}</option>
            }
          </select>
        </app-form-field>

        <app-form-field
          for="address-street"
          [label]="t().customers.detail.addresses.street"
          [required]="true"
        >
          <input
            id="address-street"
            type="text"
            class="field-control"
            [formField]="addressForm.street"
            aria-required="true"
          />
        </app-form-field>

        <app-form-field
          for="address-building"
          [label]="t().customers.detail.addresses.buildingNo"
          [required]="true"
        >
          <input
            id="address-building"
            type="text"
            class="field-control"
            [formField]="addressForm.buildingNo"
            aria-required="true"
          />
        </app-form-field>

        <app-form-field
          class="sm:col-span-2"
          for="address-description"
          [label]="t().customers.detail.addresses.description"
          [required]="true"
        >
          <textarea
            id="address-description"
            rows="3"
            class="field-control resize-y"
            [formField]="addressForm.description"
            aria-required="true"
          ></textarea>
        </app-form-field>
      </div>

      <div class="mt-8 flex items-center justify-end gap-3 border-t border-slate-100 pt-5">
        <app-button type="button" variant="outline" size="lg" (click)="cancelled.emit()">
          {{ t().customers.detail.addresses.cancel }}
        </app-button>

        <app-button type="submit" size="lg" [disabled]="addressForm().invalid()">
          <app-icon name="save" [stroke]="1.8" />
          {{ t().customers.detail.addresses.save }}
        </app-button>
      </div>
    </form>
  `
})
export class CustomerAddressForm {
  protected readonly t = inject(I18nService).t;

  /** Düzenlenecek adres; `null` ise yeni adres ekleme modudur. */
  readonly address = input<CustomerAddress | null>(null);

  readonly saved = output<AddressFormResult>();
  readonly cancelled = output<void>();

  protected readonly heading = computed(() =>
    this.address() === null
      ? this.t().customers.detail.addresses.add
      : this.t().customers.detail.addresses.editTitle
  );

  /** Düzenlenen adresin şehri listede yoksa seçenek olarak eklenir. */
  protected readonly cityOptions = computed(() => {
    const current = this.address()?.title.split(', ')[0] ?? '';
    return current !== '' && !CITIES.includes(current) ? [current, ...CITIES] : CITIES;
  });

  private readonly draft = linkedSignal<CustomerAddress | null, AddressDraft>({
    source: this.address,
    computation: (address) => toDraft(address)
  });

  protected readonly addressForm = form(this.draft, addressDraftSchema);

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.addressForm().invalid()) {
      return;
    }

    const draft = this.draft();
    this.saved.emit({
      city: draft.city,
      street: draft.street.trim(),
      buildingNo: draft.buildingNo.trim(),
      description: draft.description.trim()
    });
  }
}
