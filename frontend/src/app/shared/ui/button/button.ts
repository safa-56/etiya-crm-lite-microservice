import { Component, computed, input } from '@angular/core';

import { ButtonSize, ButtonVariant, buttonClass } from './button-classes';

/**
 * Standart aksiyon düğmesi. Host `display: contents` olduğu için gerçek `<button>`
 * elemanı üst düzenin (grid/flex) doğrudan çocuğu olarak kalır.
 */
@Component({
  selector: 'app-button',
  host: { class: 'contents' },
  template: `
    <button [type]="type()" [disabled]="disabled()" [class]="classes()">
      <ng-content />
    </button>
  `
})
export class Button {
  readonly variant = input<ButtonVariant>('primary');
  readonly size = input<ButtonSize>('md');
  readonly type = input<'button' | 'submit'>('button');
  readonly disabled = input(false);

  /**
   * Host `display: contents` olduğundan dışarıdan genişlik verilemez; blok bir kapsayıcıda
   * tam genişlik gerektiğinde bu girdi kullanılır.
   */
  readonly full = input(false);

  protected readonly classes = computed(() =>
    buttonClass(this.variant(), this.size(), false, this.full())
  );
}
