import { Component, input } from '@angular/core';

import { Icon, IconName } from '../icon/icon';

/**
 * Sol tarafında ikon taşıyan kontrol sarmalayıcısı. Kontrol içeriye yansıtılır ve
 * `.field-control pl-10` ile ikonun altına kaymayacak şekilde soldan boşluk alır;
 * sağdaki `trailing` yuvası şifre göster/gizle gibi düğmeler içindir.
 */
@Component({
  selector: 'app-input-group',
  imports: [Icon],
  host: { class: 'relative block' },
  template: `
    <span
      aria-hidden="true"
      class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400"
    >
      <app-icon [name]="icon()" />
    </span>

    <ng-content />
    <ng-content select="[trailing]" />
  `
})
export class InputGroup {
  readonly icon = input.required<IconName>();
}
