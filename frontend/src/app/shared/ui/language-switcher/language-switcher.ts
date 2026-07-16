import { Component, ElementRef, inject, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Language } from '../../../core/i18n/translations';

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
        <svg class="h-4 w-4 text-etiya-orange" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6" />
          <path
            d="M3 12h18M12 3c2.5 2.7 2.5 15.3 0 18M12 3c-2.5 2.7-2.5 15.3 0 18"
            stroke="currentColor"
            stroke-width="1.6"
          />
        </svg>
        {{ i18n.language() | uppercase }}
        <svg class="h-4 w-4 text-slate-400" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="m6 9 6 6 6-6"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
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
  imports: [UpperCasePipe],
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
