import { Component, computed, inject, input } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';

/** Shared katmanı feature'a bağımlı olmasın diye tip burada da tanımlanır. */
export type BadgeStatus = 'active' | 'passive';

/** Aktif/pasif durumunu rozet ya da nokta olarak gösterir; etiketi kendisi çevirir. */
@Component({
  selector: 'app-status-badge',
  host: { class: 'contents' },
  template: `
    @if (variant() === 'dot') {
      <span aria-hidden="true" class="h-2 w-2 rounded-full" [class]="dotClass()"></span>
      {{ label() }}
    } @else {
      <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" [class]="pillClass()">
        {{ label() }}
      </span>
    }
  `
})
export class StatusBadge {
  private readonly t = inject(I18nService).t;

  readonly status = input.required<BadgeStatus>();
  readonly variant = input<'pill' | 'dot'>('pill');

  protected readonly label = computed(() => this.t().customers.status[this.status()]);

  protected readonly pillClass = computed(() =>
    this.status() === 'active' ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'
  );

  protected readonly dotClass = computed(() =>
    this.status() === 'active' ? 'bg-emerald-500' : 'bg-slate-300'
  );
}
