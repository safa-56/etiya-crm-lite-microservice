import { Component, inject } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

import { I18nService } from '../../../core/i18n/i18n.service';

/** Giriş ekranının sol marka bölümü; dar ekranlarda gizlenir. */
@Component({
  selector: 'app-login-brand-panel',
  imports: [NgOptimizedImage],
  host: {
    class:
      'relative isolate hidden overflow-hidden bg-etiya-navy lg:flex lg:w-[55%] lg:items-center lg:justify-center'
  },
  template: `
    <div
      aria-hidden="true"
      class="pointer-events-none absolute -top-32 left-1/3 h-[420px] w-[420px] rounded-full bg-etiya-orange/25 blur-[120px]"
    ></div>
    <div
      aria-hidden="true"
      class="pointer-events-none absolute -bottom-40 -left-24 h-[520px] w-[520px] rounded-full bg-indigo-500/30 blur-[130px]"
    ></div>

    <div class="relative z-10 flex flex-col items-center px-12">
      <img
        ngSrc="etiya-logo.png"
        alt="Etiya"
        width="640"
        height="200"
        priority
        class="h-auto w-[280px] max-w-full"
      />
      <p class="mt-6 text-center text-xs font-medium tracking-[0.45em] text-white/60 uppercase">
        {{ t().login.brandTagline }}
      </p>
    </div>
  `
})
export class LoginBrandPanel {
  protected readonly t = inject(I18nService).t;
}
