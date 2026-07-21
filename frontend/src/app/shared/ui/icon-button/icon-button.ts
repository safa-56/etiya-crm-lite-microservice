import { Component, computed, input } from '@angular/core';

import { Icon, IconName } from '../icon/icon';
import { FOCUS_RING } from '../button/button-classes';

export type IconButtonTone = 'edit' | 'danger' | 'neutral' | 'ghost';

const TONES: Record<IconButtonTone, string> = {
  edit: `border border-etiya-orange/40 text-etiya-orange hover:bg-etiya-orange/10 ${FOCUS_RING}`,
  danger:
    'border border-rose-200 text-rose-600 hover:bg-rose-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-rose-500 focus-visible:ring-offset-2',
  neutral: `border border-slate-200 text-slate-500 hover:bg-etiya-gray ${FOCUS_RING}`,
  ghost: `text-slate-400 hover:bg-etiya-gray hover:text-etiya-navy ${FOCUS_RING}`
};

/** Yalnızca ikon içeren kare aksiyon düğmesi; erişilebilir ad `label` ile zorunludur. */
@Component({
  selector: 'app-icon-button',
  imports: [Icon],
  host: { class: 'contents' },
  template: `
    <button type="button" [attr.aria-label]="label()" [class]="classes()">
      <app-icon [name]="icon()" />
    </button>
  `
})
export class IconButton {
  readonly icon = input.required<IconName>();
  readonly label = input.required<string>();
  readonly tone = input<IconButtonTone>('neutral');

  protected readonly classes = computed(() => {
    const box = this.tone() === 'ghost' ? 'h-7 w-7' : 'h-8 w-8';
    return `flex ${box} items-center justify-center rounded-md transition ${TONES[this.tone()]}`;
  });
}
