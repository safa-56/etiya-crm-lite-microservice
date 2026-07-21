import { Component, computed, input } from '@angular/core';

import { Icon, IconName } from '../icon/icon';

export type EmptyStateSize = 'sm' | 'md' | 'lg';

const SIZES: Record<EmptyStateSize, string> = {
  sm: 'rounded-lg border-slate-200 px-5 py-6',
  md: 'rounded-xl border-slate-200 px-6 py-12',
  lg: 'min-h-55 rounded-2xl border-slate-200 bg-white px-6 py-16'
};

/** Kesikli çerçeveli "kayıt yok" bloğu; isteğe bağlı ikon ve aksiyon alır. */
@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  host: { '[class]': 'classes()' },
  template: `
    @if (icon(); as name) {
      <span
        aria-hidden="true"
        class="flex h-12 w-12 items-center justify-center rounded-full bg-etiya-gray text-slate-400"
      >
        <app-icon [name]="name" size="h-5 w-5" [stroke]="1.8" />
      </span>
    }

    <p class="text-sm text-slate-500">{{ message() }}</p>

    <ng-content />
  `
})
export class EmptyState {
  readonly message = input.required<string>();
  readonly icon = input<IconName | null>(null);
  readonly size = input<EmptyStateSize>('md');

  protected readonly classes = computed(
    () =>
      `flex flex-col items-center justify-center gap-4 border border-dashed text-center ${SIZES[this.size()]}`
  );
}
