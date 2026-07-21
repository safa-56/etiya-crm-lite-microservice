import { Component, inject, input } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

import { I18nService } from '../../core/i18n/i18n.service';
import { PageKey } from '../../core/i18n/translations';
import { SidebarItem } from './sidebar-item';

/** Sol gezinme sütunu: logo, menü ve sürüm bilgisi. */
@Component({
  selector: 'app-sidebar',
  imports: [NgOptimizedImage, SidebarItem],
  host: { class: 'fixed inset-y-0 left-0 z-30 hidden w-60 flex-col bg-etiya-navy lg:flex' },
  template: `
    <div class="flex h-16 shrink-0 items-center justify-center border-b border-white/10 px-6">
      <img
        ngSrc="etiya-logo.png"
        alt="Etiya"
        width="640"
        height="200"
        priority
        class="h-auto w-24"
      />
    </div>

    <nav class="flex-1 overflow-y-auto px-3 py-5" [attr.aria-label]="t().shell.mainNavigation">
      <ul class="flex flex-col gap-1">
        <li>
          <app-sidebar-item
            [label]="t().nav.customerSearch"
            icon="search"
            link="/customers"
            [exact]="true"
          />
        </li>

        <!-- Müşteri detayı yalnızca arama sonucundan açılır; menüde aktif durumu gösterilir. -->
        <li>
          <app-sidebar-item
            [label]="t().nav.customerInfo"
            icon="user"
            [active]="pageKey() === 'customerDetail'"
          />
        </li>

        <li>
          <app-sidebar-item
            [label]="t().nav.customerCreate"
            icon="user-plus"
            link="/customers/new"
          />
        </li>

        <li><app-sidebar-item [label]="t().nav.orders" icon="document" /></li>
        <li><app-sidebar-item [label]="t().nav.reports" icon="chart" /></li>
        <li><app-sidebar-item [label]="t().nav.settings" icon="settings" /></li>

        <li>
          <app-sidebar-item [label]="t().nav.logout" icon="logout" link="/login" [accent]="true" />
        </li>
      </ul>
    </nav>

    <p class="shrink-0 border-t border-white/10 px-6 py-4 text-[11px] text-white/40">
      {{ t().shell.version }}
    </p>
  `
})
export class Sidebar {
  protected readonly t = inject(I18nService).t;

  /** Menüde bağlantısı olmayan sayfaların aktifliğini belirler. */
  readonly pageKey = input.required<PageKey>();
}
