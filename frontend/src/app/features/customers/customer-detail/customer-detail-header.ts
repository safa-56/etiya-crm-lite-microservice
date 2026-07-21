import { Component, computed, inject, input } from '@angular/core';
import { formatDate } from '@angular/common';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Avatar } from '../../../shared/ui/avatar/avatar';
import { StatusBadge } from '../../../shared/ui/status-badge/status-badge';
import { Customer, customerDisplayName, customerInitials } from '../customer.model';

/** Detay sayfasının kimlik kartı: avatar, ad, rol ve durum rozetleri. */
@Component({
  selector: 'app-customer-detail-header',
  imports: [Avatar, StatusBadge],
  host: { class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm' },
  template: `
    <div class="flex flex-wrap items-center gap-4">
      <app-avatar [initials]="initials()" size="lg" />

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-center gap-3">
          <h2 class="text-xl font-bold text-etiya-navy">{{ displayName() }}</h2>
          <span class="rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700">
            {{ t().customers.detail.role }}
          </span>
          <app-status-badge [status]="customer().status" />
        </div>
        <p class="mt-1 text-sm text-slate-500">
          {{ customer().code }} · {{ customer().type }} · {{ t().customers.detail.role }} ·
          {{ memberSince() }}
        </p>
      </div>
    </div>
  `
})
export class CustomerDetailHeader {
  private readonly i18n = inject(I18nService);

  protected readonly t = this.i18n.t;

  readonly customer = input.required<Customer>();

  protected readonly displayName = computed(() => customerDisplayName(this.customer()));
  protected readonly initials = computed(() => customerInitials(this.customer()));

  /** "Temmuz 2021'den beri" gibi üyelik metni; ay adı aktif dile göre biçimlenir. */
  protected readonly memberSince = computed(() => {
    const monthYear = formatDate(this.customer().registeredAt, 'LLLL yyyy', this.i18n.language());
    return this.t().customers.detail.memberSince.replace('{date}', monthYear);
  });
}
