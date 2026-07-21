import { Component, ElementRef, inject, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Language } from '../../../core/i18n/translations';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-language-switcher',
  template: `
    <div class="relative">
      <button
        type="button"
        (click)="toggle()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="i18n.t().common.languageSelector"
        aria-haspopup="listbox"
        class="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-medium text-etiya-navy shadow-sm transition hover:border-slate-300 hover:bg-etiya-gray focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
      >
        <span class="text-etiya-orange"><app-icon name="globe" /></span>
        {{ i18n.language() | uppercase }}
        <span class="text-slate-400"><app-icon name="chevron-down" [stroke]="1.8" /></span>
      </button>

      @if (open()) {
        <ul
          role="listbox"
          class="absolute right-0 z-20 mt-2 w-28 overflow-hidden rounded-lg border border-slate-200 bg-white py-1 shadow-lg"
        >
          @for (language of i18n.availableLanguages; track language) {
            <li role="option" [attr.aria-selected]="i18n.language() === language">
              <button
                type="button"
                (click)="select(language)"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm text-etiya-navy transition hover:bg-etiya-gray focus:outline-none focus-visible:bg-etiya-gray"
              >
                {{ language | uppercase }}
                @if (i18n.language() === language) {
                  <span class="text-etiya-orange" aria-hidden="true">•</span>
                }
              </button>
            </li>
          }
        </ul>
      }
    </div>
  `,
  imports: [UpperCasePipe, Icon],
  host: {
    '(document:click)': 'onDocumentClick($event)'
  }
})
export class LanguageSwitcher {
  protected readonly i18n = inject(I18nService);
  protected readonly open = signal(false);

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  // Butona tıklanınca menüyü aç/kapa
  protected toggle(): void {
    this.open.update((open) => !open);
  }

  // bir dil seçilince servise bildir ve menüyü kapat
  protected select(language: Language): void {
    this.i18n.setLanguage(language);
    this.open.set(false);
  }

  // dil menüsü dışına tıklanınca kapan mantığı
  protected onDocumentClick(event: MouseEvent): void {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }
}
