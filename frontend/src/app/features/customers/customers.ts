import { Component, inject, signal } from '@angular/core';

import { I18nService } from '../../core/i18n/i18n.service';
import { EmptyState } from '../../shared/ui/empty-state/empty-state';
import { Customer } from './customer.model';
import { CustomerService } from './customer.service';
import { CustomerResultsTable } from './customer-results-table/customer-results-table';
import {
  CustomerSearchFiltersPanel,
  CustomerSearchRequest
} from './customer-search-filters/customer-search-filters';

/** Arama sayfası; filtre panelini servise, sonucu tabloya bağlar. */
@Component({
  selector: 'app-customers',
  imports: [CustomerSearchFiltersPanel, CustomerResultsTable, EmptyState],
  templateUrl: './customers.html'
})
export class Customers {
  private readonly customers = inject(CustomerService);

  protected readonly t = inject(I18nService).t;

  /**
   * Filtre paneli açılışta boş filtrelerle bir arama tetikler, bu yüzden liste ilk
   * render'da zaten dolu gelir.
   */
  protected readonly results = signal<readonly Customer[]>([]);

  protected onSearch(request: CustomerSearchRequest): void {
    this.results.set(this.customers.search(request.type, request.filters));
  }
}
