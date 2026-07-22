import { Component, ElementRef, afterNextRender, inject, input, output } from '@angular/core';

import { Button } from '../button/button';
import { Icon } from '../icon/icon';

/**
 * Modal onay penceresi. Arka planı karartır, içeriği ortalar ve odağı içeride tutmak yerine
 * açılışta onay düğmesine taşır. Escape veya arka plana tıklama iptal eder.
 */
@Component({
  selector: 'app-confirm-dialog',
  imports: [Button, Icon],
  host: {
    class:
      'fixed inset-0 z-50 flex items-center justify-center bg-etiya-navy/40 px-4 backdrop-blur-sm',
    role: 'presentation',
    '(click)': 'onBackdrop($event)',
    '(keydown.escape)': 'cancelled.emit()'
  },
  template: `
    <div
      role="alertdialog"
      aria-modal="true"
      [attr.aria-label]="message()"
      class="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"
    >
      <div class="flex items-center gap-4">
        <span
          aria-hidden="true"
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-rose-50 text-rose-600"
        >
          <app-icon name="alert" size="h-6 w-6" />
        </span>
        <p class="text-base font-semibold text-etiya-navy">{{ message() }}</p>
      </div>

      <div class="mt-6 flex items-center justify-center gap-3">
        <app-button variant="outline" size="lg" (click)="cancelled.emit()">
          {{ cancelLabel() }}
        </app-button>
        <app-button variant="danger" size="lg" (click)="confirmed.emit()">
          {{ confirmLabel() }}
        </app-button>
      </div>
    </div>
  `
})
export class ConfirmDialog {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly message = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  constructor() {
    afterNextRender(() => {
      this.host.nativeElement.querySelector<HTMLButtonElement>('button')?.focus();
    });
  }

  /** Yalnızca arka plana (karartma katmanına) tıklanırsa kapatır. */
  protected onBackdrop(event: MouseEvent): void {
    if (event.target === this.host.nativeElement) {
      this.cancelled.emit();
    }
  }
}
