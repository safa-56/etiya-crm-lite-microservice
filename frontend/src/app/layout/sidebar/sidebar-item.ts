import { Component, computed, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { Icon, IconName } from '../../shared/ui/icon/icon';

const BASE =
  'relative flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm transition hover:bg-white/10 hover:text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2 focus-visible:ring-offset-etiya-navy';

/**
 * Sol menü satırı. `link` verilmezse henüz sayfası olmayan menü başlıkları için düğme
 * üretir; aktiflik ya `routerLinkActive` ile ya da `active` girdisiyle belirlenir.
 */
@Component({
  selector: 'app-sidebar-item',
  imports: [RouterLink, RouterLinkActive, Icon],
  host: { class: 'block' },
  template: `
    @if (link(); as target) {
      <a
        [routerLink]="target"
        routerLinkActive
        [routerLinkActiveOptions]="{ exact: exact() }"
        #linkActive="routerLinkActive"
        [class]="classes(linkActive.isActive)"
        [attr.aria-current]="linkActive.isActive ? 'page' : null"
      >
        @if (linkActive.isActive) {
          <span
            aria-hidden="true"
            class="absolute inset-y-1.5 left-0 w-1 rounded-r bg-etiya-orange"
          ></span>
        }
        <app-icon [name]="icon()" size="h-4 w-4 shrink-0" />
        {{ label() }}
      </a>
    } @else {
      <button
        type="button"
        [class]="classes(active())"
        [attr.aria-current]="active() ? 'page' : null"
      >
        @if (active()) {
          <span
            aria-hidden="true"
            class="absolute inset-y-1.5 left-0 w-1 rounded-r bg-etiya-orange"
          ></span>
        }
        <app-icon [name]="icon()" size="h-4 w-4 shrink-0" />
        {{ label() }}
      </button>
    }
  `
})
export class SidebarItem {
  readonly label = input.required<string>();
  readonly icon = input.required<IconName>();
  readonly link = input<string | null>(null);

  /** `link` verilmediğinde aktifliği dışarıdan belirlemek için. */
  readonly active = input(false);
  readonly exact = input(false);

  /** Çıkış gibi vurgulu satırlar turuncu yazılır. */
  readonly accent = input(false);

  protected classes(isActive: boolean): string {
    if (this.accent()) {
      return `${BASE} font-semibold text-etiya-orange`;
    }

    return `${BASE} font-medium ${isActive ? 'bg-white/10 text-white' : 'text-white/70'}`;
  }
}
