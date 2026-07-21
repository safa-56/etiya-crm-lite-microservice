import { Component, input } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

import { LanguageSwitcher } from '../../shared/ui/language-switcher/language-switcher';
import { CurrentUser, UserMenu } from './user-menu';

/** Sayfa başlığı, dil seçimi ve kullanıcı menüsünü taşıyan yapışkan üst bar. */
@Component({
  selector: 'app-topbar',
  imports: [NgOptimizedImage, LanguageSwitcher, UserMenu],
  host: {
    class:
      'sticky top-0 z-20 flex flex-wrap items-center justify-between gap-4 border-b border-slate-200 bg-white px-6 py-3'
  },
  template: `
    <div class="flex items-center gap-3">
      <img
        ngSrc="etiya-logo-2.png"
        alt="Etiya"
        width="910"
        height="223"
        class="h-auto w-16 lg:hidden"
      />
      <div>
        <h1 class="text-lg font-bold text-etiya-navy">{{ title() }}</h1>
        <p class="text-xs text-slate-400">{{ subtitle() }}</p>
      </div>
    </div>

    <div class="flex items-center gap-4">
      <app-language-switcher />

      <span aria-hidden="true" class="hidden h-8 w-px bg-slate-200 sm:block"></span>

      <app-user-menu [user]="user()" />
    </div>
  `
})
export class Topbar {
  readonly title = input.required<string>();
  readonly subtitle = input.required<string>();
  readonly user = input.required<CurrentUser>();
}
