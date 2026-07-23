import { Component, inject, input, output, signal } from '@angular/core';

import { I18nService } from '../../core/i18n/i18n.service';
import { Avatar } from '../../shared/ui/avatar/avatar';
import { Icon } from '../../shared/ui/icon/icon';

export interface CurrentUser {
  readonly name: string;
  readonly role: string;
  readonly initials: string;
}

/** Üst bardaki kullanıcı düğmesi ve oturum menüsü. */
@Component({
  selector: 'app-user-menu',
  imports: [Avatar, Icon],
  host: {
    class: 'relative',
    // Menü dışına tıklanınca ya da Escape'e basılınca kapanır.
    '(document:click)': 'close()',
    '(document:keydown.escape)': 'close()'
  },
  template: `
    <button
      type="button"
      [attr.aria-label]="t().shell.userMenu"
      aria-haspopup="menu"
      [attr.aria-expanded]="open()"
      (click)="toggle($event)"
      class="flex items-center gap-3 rounded-lg px-1.5 py-1 transition hover:bg-etiya-gray focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
    >
      <app-avatar [initials]="user().initials" size="md" />

      <span class="hidden text-left sm:block">
        <span class="block text-sm font-semibold text-etiya-navy">{{ user().name }}</span>
        <span class="block text-xs text-slate-400">{{ user().role }}</span>
      </span>

      <span class="text-slate-400">
        <app-icon name="chevron-down" [stroke]="1.8" />
      </span>
    </button>

    @if (open()) {
      <div
        role="menu"
        class="absolute right-0 z-30 mt-2 w-48 overflow-hidden rounded-lg border border-slate-200 bg-white py-1 shadow-lg"
      >
        <button
          type="button"
          role="menuitem"
          (click)="logout.emit()"
          class="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-etiya-navy transition hover:bg-etiya-gray focus:outline-none focus-visible:bg-etiya-gray"
        >
          {{ t().shell.logout }}
        </button>
      </div>
    }
  `
})
export class UserMenu {
  protected readonly t = inject(I18nService).t;

  readonly user = input.required<CurrentUser>();

  /** Kullanıcı çıkış yapmak istedi; oturumu kapatmak kabuğun işidir. */
  readonly logout = output<void>();

  protected readonly open = signal(false);

  protected toggle(event: MouseEvent): void {
    // Aksi hâlde document dinleyicisi aynı tıklamada menüyü hemen kapatırdı.
    event.stopPropagation();
    this.open.update((isOpen) => !isOpen);
  }

  protected close(): void {
    this.open.set(false);
  }
}
