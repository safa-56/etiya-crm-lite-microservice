import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

import { I18nService } from '../../core/i18n/i18n.service';
import { EmptyState } from '../../shared/ui/empty-state/empty-state';
import { CustomerSearchResult, CustomerService } from './customer.service';
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

  /** Bir sayfada gösterilen kayıt sayısı (search-service üst sınırı içinde). */
  private static readonly PAGE_SIZE = 15;

  /** Son başarılı/başarısız arama sonucu; henüz arama yapılmadıysa null. */
  protected readonly result = signal<CustomerSearchResult | null>(null);
  protected readonly loading = signal(false);
  /** Backend'in reddettiği (400) veya ulaşılamayan aramada gösterilecek mesaj. */
  protected readonly errorMessage = signal<string | null>(null);

  /** Sayfa değiştirilirken aynı kriterle yeniden sorgulamak için son istek saklanır. */
  private lastRequest: CustomerSearchRequest | null = null;

  /** Filtre panelinden yeni arama; her zaman ilk sayfadan başlar. */
  protected onSearch(request: CustomerSearchRequest): void {
    this.lastRequest = request;
    this.fetch(request, 0);
  }

  /** Sonuç tablosundaki sayfa değişimi; son kriterle istenen sayfayı çeker. */
  protected onPageChange(page: number): void {
    if (this.lastRequest !== null) {
      this.fetch(this.lastRequest, page);
    }
  }

  private fetch(request: CustomerSearchRequest, page: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.customers.search(request.type, request.filters, page, Customers.PAGE_SIZE).subscribe({
      next: (result) => {
        this.loading.set(false);
        this.result.set(result);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.result.set(null);
        this.errorMessage.set(this.toErrorMessage(error));
      }
    });
  }

  /**
   * Backend'in 400 doğrulama yanıtındaki ({@code ProblemDetail.detail}) dile çözülmüş
   * mesajı kullanır; başka hatalarda genel bir mesaja düşer.
   */
  private toErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === 400 && error.error) {
      const detail = (error.error as { detail?: string }).detail;
      if (typeof detail === 'string' && detail.trim() !== '') {
        return detail;
      }
    }
    return this.t().customers.results.loadError;
  }
}
