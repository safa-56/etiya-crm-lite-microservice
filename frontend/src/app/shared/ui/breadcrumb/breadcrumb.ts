import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Icon } from '../icon/icon';

export interface BreadcrumbItem {
  readonly label: string;
  /** Son kırıntıda bağlantı olmaz; aktif sayfayı temsil eder. */
  readonly link?: string;
}

@Component({
  selector: 'app-breadcrumb',
  imports: [RouterLink, Icon],
  host: { role: 'navigation', '[attr.aria-label]': 'label()' },
  template: `
    <ol class="flex flex-wrap items-center gap-2 text-sm">
      @for (item of items(); track item.label; let last = $last) {
        <li [attr.aria-current]="last ? 'page' : null" [class]="last ? 'text-slate-500' : ''">
          @if (item.link; as link) {
            <a
              [routerLink]="link"
              class="font-semibold text-etiya-orange transition hover:text-etiya-orange-dark hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
            >
              {{ item.label }}
            </a>
          } @else {
            {{ item.label }}
          }
        </li>

        @if (!last) {
          <li aria-hidden="true" class="text-slate-300">
            <app-icon name="chevron-right" [stroke]="1.8" />
          </li>
        }
      }
    </ol>
  `
})
export class Breadcrumb {
  readonly items = input.required<readonly BreadcrumbItem[]>();
  readonly label = input.required<string>();
}
