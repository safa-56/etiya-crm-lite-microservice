import { Component, computed, inject } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  ActivatedRouteSnapshot,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';
import { filter, map } from 'rxjs';

import { I18nService } from '../../core/i18n/i18n.service';
import { PageKey } from '../../core/i18n/translations';
import { LanguageSwitcher } from '../../shared/ui/language-switcher/language-switcher';

const DEFAULT_PAGE: PageKey = 'customers';

@Component({
  selector: 'app-main-layout',
  imports: [NgOptimizedImage, RouterLink, RouterLinkActive, RouterOutlet, LanguageSwitcher],
  templateUrl: './main-layout.html'
})
export class MainLayout {
  private readonly router = inject(Router);

  protected readonly t = inject(I18nService).t;

  /** Tasarım aşamasında sabit kullanıcı; oturum servisi bağlanınca değişecek. */
  protected readonly user = { name: 'Ahmet Kaya', role: 'CRM Uzmanı', initials: 'AK' };

  /** Aktif route'un `data.pageKey` değeri; üst başlığı belirler. */
  protected readonly pageKey = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.readPageKey())
    ),
    { initialValue: this.readPageKey() }
  );

  protected readonly page = computed(() => this.t().pages[this.pageKey()]);

  /**
   * Bileşen, çocuk route aktive edilmeden önce oluşturulduğu için `ActivatedRoute.firstChild`
   * üzerinden snapshot okunamaz; router'ın hazır durum ağacı taranır.
   */
  private readPageKey(): PageKey {
    let snapshot: ActivatedRouteSnapshot | null = this.router.routerState.snapshot.root;
    let key = '';

    while (snapshot) {
      const value = snapshot.data['pageKey'];

      if (typeof value === 'string') {
        key = value;
      }

      snapshot = snapshot.firstChild;
    }

    return key in this.t().pages ? (key as PageKey) : DEFAULT_PAGE;
  }
}
