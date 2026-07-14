import { computed, effect, inject, PLATFORM_ID, Service, signal } from '@angular/core';
import { DOCUMENT, isPlatformBrowser } from '@angular/common';

import { DEFAULT_LANGUAGE, LANGUAGES, Language, TRANSLATIONS } from './translations';

const STORAGE_KEY = 'etiya.language';

@Service()
export class I18nService {
  private readonly document = inject(DOCUMENT);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly currentLanguage = signal<Language>(this.readStoredLanguage());

  /** Aktif dil. */
  readonly language = this.currentLanguage.asReadonly();

  /** Aktif dile ait sözlük; şablonlarda `t().login.title` şeklinde okunur. */
  readonly t = computed(() => TRANSLATIONS[this.currentLanguage()]);

  /** Dropdown gibi seçim bileşenleri için desteklenen diller. */
  readonly availableLanguages = LANGUAGES;

  constructor() {
    effect(() => {
      const language = this.currentLanguage();
      this.document.documentElement.lang = language;

      if (this.isBrowser) {
        localStorage.setItem(STORAGE_KEY, language);
      }
    });
  }

  setLanguage(language: Language): void {
    this.currentLanguage.set(language);
  }

  private readStoredLanguage(): Language {
    if (!this.isBrowser) {
      return DEFAULT_LANGUAGE;
    }

    const stored = localStorage.getItem(STORAGE_KEY);
    return this.isSupported(stored) ? stored : DEFAULT_LANGUAGE;
  }

  private isSupported(value: string | null): value is Language {
    return value !== null && (LANGUAGES as readonly string[]).includes(value);
  }
}
