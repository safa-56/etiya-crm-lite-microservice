import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRouteSnapshot, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { userInitials } from '../../core/auth/auth.model';
import { I18nService } from '../../core/i18n/i18n.service';
import { PageKey } from '../../core/i18n/translations';
import { ShellTitle } from '../shell-title';
import { Sidebar } from '../sidebar/sidebar';
import { Topbar } from '../topbar/topbar';
import { CurrentUser } from '../topbar/user-menu';

const DEFAULT_PAGE: PageKey = 'customers';

/** Oturum beklenmedik şekilde boşsa üst barın çökmemesi için nötr kullanıcı. */
const ANONYMOUS_USER: CurrentUser = { name: '—', role: '', initials: '' };

/** Kabuk yalnızca yerleşimi kurar; menü ve üst bar kendi bileşenlerinde yaşar. */
@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, Sidebar, Topbar],
  templateUrl: './main-layout.html'
})
export class MainLayout {
  private readonly router = inject(Router);
  private readonly shellTitle = inject(ShellTitle);
  private readonly authStore = inject(AuthStore);
  private readonly auth = inject(AuthService);

  private readonly t = inject(I18nService).t;

  /** Üst barda gösterilen kullanıcı; access token'daki claim'lerden türetilir. */
  protected readonly user = computed<CurrentUser>(() => {
    const user = this.authStore.user();

    if (user === null) {
      return ANONYMOUS_USER;
    }

    return {
      name: user.displayName,
      role: this.roleLabel(user.roles),
      initials: userInitials(user)
    };
  });

  /** Oturumu kapatır ve giriş ekranına döner. */
  protected logout(): void {
    this.auth.logout().subscribe(() => {
      void this.router.navigate(['/login']);
    });
  }

  /**
   * Kullanıcının ilk tanınan realm rolünü ekran etiketine çevirir; sözlükte
   * karşılığı yoksa ham rol kodu gösterilir.
   */
  private roleLabel(roles: readonly string[]): string {
    const labels = this.t().shell.roles;
    const known = roles.find((role) => role in labels);

    return known === undefined ? (roles[0] ?? '') : labels[known as keyof typeof labels];
  }

  /** Aktif route'un `data.pageKey` değeri; üst başlığı belirler. */
  protected readonly pageKey = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.readPageKey())
    ),
    { initialValue: this.readPageKey() }
  );

  protected readonly page = computed(
    () => this.shellTitle.override() ?? this.t().pages[this.pageKey()]
  );

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
