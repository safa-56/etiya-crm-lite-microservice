import { Component, input, output } from '@angular/core';

/** Tek sayfa kaldığında hiçbir şey çizmeyen basit sayfa numarası gezinmesi. */
@Component({
  selector: 'app-pagination',
  template: `
    @if (pages().length > 1) {
      <nav [attr.aria-label]="label()" class="mt-5 flex justify-center">
        <ul class="flex items-center gap-2">
          @for (page of pages(); track page) {
            <li>
              <button
                type="button"
                (click)="pageChange.emit(page)"
                [attr.aria-current]="current() === page ? 'page' : null"
                [attr.aria-label]="pageLabel() + ' ' + page"
                class="flex h-9 w-9 items-center justify-center rounded-lg border text-sm font-semibold transition focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
                [class]="
                  current() === page
                    ? 'border-etiya-orange bg-etiya-orange text-white'
                    : 'border-slate-200 bg-white text-etiya-navy hover:bg-etiya-gray'
                "
              >
                {{ page }}
              </button>
            </li>
          }
        </ul>
      </nav>
    }
  `
})
export class Pagination {
  readonly pages = input.required<readonly number[]>();
  readonly current = input.required<number>();
  readonly label = input.required<string>();
  readonly pageLabel = input.required<string>();

  readonly pageChange = output<number>();
}
