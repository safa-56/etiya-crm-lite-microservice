import { Component, input } from '@angular/core';

/** Uygulamada kullanılan tüm SVG ikonlarının adları. */
export type IconName =
  | 'search'
  | 'chevron-right'
  | 'chevron-down'
  | 'arrow-right'
  | 'plus'
  | 'close'
  | 'pencil'
  | 'trash'
  | 'info'
  | 'pin'
  | 'dots'
  | 'user'
  | 'user-plus'
  | 'document'
  | 'chart'
  | 'settings'
  | 'logout'
  | 'globe'
  | 'lock'
  | 'eye'
  | 'eye-off';

/**
 * Tek SVG kaynağı. `display: contents` sayesinde saran etiket düzeni etkilemez,
 * renk `currentColor` üzerinden üst elemandan devralınır.
 */
@Component({
  selector: 'app-icon',
  host: { class: 'contents' },
  template: `
    <svg [class]="size()" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      @switch (name()) {
        @case ('search') {
          <circle cx="11" cy="11" r="6.5" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="m16 16 4.5 4.5"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('chevron-right') {
          <path
            d="m9 6 6 6-6 6"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        }
        @case ('chevron-down') {
          <path
            d="m6 9 6 6 6-6"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        }
        @case ('arrow-right') {
          <path
            d="M5 12h14m-6-6 6 6-6 6"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        }
        @case ('plus') {
          <path
            d="M12 6v12M6 12h12"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('close') {
          <path
            d="m6 6 12 12M18 6 6 18"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('pencil') {
          <path
            d="M4 20h4L19 9a2 2 0 0 0-3-3L5 17v3Z"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linejoin="round"
          />
        }
        @case ('trash') {
          <path
            d="M6 7h12M10 7V5h4v2m-6 0v12h8V7M10.5 10v6m3-6v6"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        }
        @case ('info') {
          <circle cx="12" cy="12" r="7.5" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="M12 11v5m0-8.5v.5"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('pin') {
          <path
            d="M12 21s6.5-5.4 6.5-10a6.5 6.5 0 1 0-13 0c0 4.6 6.5 10 6.5 10Z"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linejoin="round"
          />
          <circle cx="12" cy="11" r="2.2" stroke="currentColor" [attr.stroke-width]="stroke()" />
        }
        @case ('dots') {
          <circle cx="12" cy="5.5" r="1.5" fill="currentColor" />
          <circle cx="12" cy="12" r="1.5" fill="currentColor" />
          <circle cx="12" cy="18.5" r="1.5" fill="currentColor" />
        }
        @case ('user') {
          <circle cx="12" cy="8" r="3.5" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="M5 19.5a7 7 0 0 1 14 0"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('user-plus') {
          <circle cx="10" cy="8" r="3.5" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="M3.5 19.5a6.5 6.5 0 0 1 13 0M18 8.5v5M15.5 11h5"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('document') {
          <path
            d="M5 6h14v13a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6ZM9 4h6v2H9V4Zm-.5 8h7m-7 4h4"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('chart') {
          <path
            d="M5 19V5m0 14h14M8.5 16V9.5m4 6.5v-10m4 10v-6"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('settings') {
          <circle cx="12" cy="12" r="3" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="M4.5 12a7.5 7.5 0 0 1 .3-2l-1.6-1.4 1.8-3.1 2 .8a7.5 7.5 0 0 1 1.7-1l.3-2.1h3.6l.3 2.1c.6.3 1.2.6 1.7 1l2-.8 1.8 3.1-1.6 1.4a7.5 7.5 0 0 1 0 4l1.6 1.4-1.8 3.1-2-.8c-.5.4-1.1.7-1.7 1l-.3 2.1h-3.6l-.3-2.1a7.5 7.5 0 0 1-1.7-1l-2 .8-1.8-3.1L4.8 14a7.5 7.5 0 0 1-.3-2Z"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linejoin="round"
          />
        }
        @case ('logout') {
          <path
            d="M14 8V6a1 1 0 0 0-1-1H6a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h7a1 1 0 0 0 1-1v-2m2-8 3 4-3 4m3-4H9"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        }
        @case ('lock') {
          <rect
            x="4"
            y="10"
            width="16"
            height="10"
            rx="2"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
          />
          <path d="M8 10V7a4 4 0 1 1 8 0v3" stroke="currentColor" [attr.stroke-width]="stroke()" />
        }
        @case ('eye') {
          <path
            d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
          />
          <circle cx="12" cy="12" r="3" stroke="currentColor" [attr.stroke-width]="stroke()" />
        }
        @case ('eye-off') {
          <path
            d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
          />
          <circle cx="12" cy="12" r="3" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="m4 4 16 16"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
            stroke-linecap="round"
          />
        }
        @case ('globe') {
          <circle cx="12" cy="12" r="9" stroke="currentColor" [attr.stroke-width]="stroke()" />
          <path
            d="M3 12h18M12 3c2.5 2.7 2.5 15.3 0 18M12 3c-2.5 2.7-2.5 15.3 0 18"
            stroke="currentColor"
            [attr.stroke-width]="stroke()"
          />
        }
      }
    </svg>
  `
})
export class Icon {
  readonly name = input.required<IconName>();

  /** Tailwind boyut sınıfı; ikonlar varsayılan olarak 16px kare çizilir. */
  readonly size = input('h-4 w-4');

  readonly stroke = input(1.6);
}
