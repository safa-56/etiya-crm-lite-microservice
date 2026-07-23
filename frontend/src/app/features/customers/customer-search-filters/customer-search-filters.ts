import { Component, OnInit, computed, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { I18nService } from '../../../core/i18n/i18n.service';
import { DigitsOnly } from '../../../shared/directives/digits-only';
import { LettersOnly } from '../../../shared/directives/letters-only';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { CustomerType } from '../customer.model';
import { CustomerSearchFilters } from '../customer.service';
import { CustomerTypeToggle } from './customer-type-toggle';

export interface CustomerSearchRequest {
  readonly type: CustomerType;
  readonly filters: CustomerSearchFilters;
}

/** Arama panelinin tamamı: müşteri tipi, filtre alanları ve aksiyonlar. */
@Component({
  selector: 'app-customer-search-filters',
  imports: [
    ReactiveFormsModule,
    Button,
    FormFieldShell,
    Icon,
    CustomerTypeToggle,
    DigitsOnly,
    LettersOnly
  ],
  templateUrl: './customer-search-filters.html'
})
export class CustomerSearchFiltersPanel implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  protected readonly t = inject(I18nService).t;

  /** Boş filtrelerle çalıştığında seçili tipin tüm müşterilerini döndürür. */
  readonly search = output<CustomerSearchRequest>();

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

  protected readonly isCorporate = computed(() => this.customerType() === 'B2B');

  /** Sayfa açılır açılmaz filtresiz arama yapılır; liste boş değil, dolu gelir. */
  ngOnInit(): void {
    this.submit();
  }

  /** Tip değişince önceki tipe ait filtreler anlamsız kalır. */
  protected setCustomerType(type: CustomerType): void {
    if (this.customerType() === type) {
      return;
    }

    this.customerType.set(type);
    this.clear();
  }

  protected submit(): void {
    this.search.emit({ type: this.customerType(), filters: this.form.getRawValue() });
  }

  /** Filtreler sıfırlandığında daraltma kalmaz; yeniden tüm liste gösterilir. */
  protected clear(): void {
    this.form.reset();
    this.submit();
  }
}
