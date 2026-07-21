import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ButtonSize, ButtonVariant, buttonClass } from './button-classes';

/** Düğme görünümlü yönlendirme bağlantısı; gezinme anlamı taşıdığı için `<a>` üretir. */
@Component({
  selector: 'app-button-link',
  imports: [RouterLink],
  host: { class: 'contents' },
  template: `
    <a [routerLink]="link()" [class]="classes()">
      <ng-content />
    </a>
  `
})
export class ButtonLink {
  readonly link = input.required<string | readonly unknown[]>();
  readonly variant = input<ButtonVariant>('primary');
  readonly size = input<ButtonSize>('md');

  protected readonly classes = computed(() => buttonClass(this.variant(), this.size(), true));
}
