import { Component, inject, input } from '@angular/core';

import { I18nService } from '../../core/i18n/i18n.service';
import { Avatar } from '../../shared/ui/avatar/avatar';
import { Icon } from '../../shared/ui/icon/icon';

export interface CurrentUser {
  readonly name: string;
  readonly role: string;
  readonly initials: string;
}

/** Üst bardaki kullanıcı düğmesi; açılır menüsü oturum servisiyle birlikte eklenecek. */
@Component({
  selector: 'app-user-menu',
  imports: [Avatar, Icon],
  template: `
    <button
      type="button"
      [attr.aria-label]="t().shell.userMenu"
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
  `
})
export class UserMenu {
  protected readonly t = inject(I18nService).t;

  readonly user = input.required<CurrentUser>();
}
