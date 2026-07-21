import { Component, ElementRef, input, output, viewChildren } from '@angular/core';

export interface TabItem {
  readonly id: string;
  readonly label: string;
}

/**
 * WAI-ARIA sekme listesi. Sekmeler arasında yalnızca aktif olan Tab sırasındadır; geçiş
 * ok tuşlarıyla yapılır (roving tabindex).
 */
@Component({
  selector: 'app-tabs',
  host: {
    role: 'tablist',
    class: 'flex flex-wrap gap-3',
    '[attr.aria-label]': 'label()',
    '(keydown)': 'onKeydown($event)'
  },
  template: `
    @for (tab of tabs(); track tab.id) {
      <button
        #tabButton
        type="button"
        role="tab"
        [id]="'tab-' + tab.id"
        [attr.aria-controls]="'panel-' + tab.id"
        [attr.aria-selected]="active() === tab.id"
        [attr.tabindex]="active() === tab.id ? 0 : -1"
        (click)="activeChange.emit(tab.id)"
        class="rounded-lg border bg-white px-5 py-2.5 text-sm font-semibold shadow-sm transition focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
        [class]="
          active() === tab.id
            ? 'border-etiya-navy text-etiya-navy'
            : 'border-slate-200 text-slate-600 hover:border-slate-300 hover:text-etiya-navy'
        "
      >
        {{ tab.label }}
      </button>
    }
  `
})
export class Tabs {
  readonly tabs = input.required<readonly TabItem[]>();
  readonly active = input.required<string>();
  readonly label = input.required<string>();

  readonly activeChange = output<string>();

  private readonly tabButtons = viewChildren<ElementRef<HTMLButtonElement>>('tabButton');

  protected onKeydown(event: KeyboardEvent): void {
    const items = this.tabs();
    const current = items.findIndex((tab) => tab.id === this.active());

    if (current === -1) {
      return;
    }

    let next: number;

    switch (event.key) {
      case 'ArrowRight':
        next = (current + 1) % items.length;
        break;
      case 'ArrowLeft':
        next = (current - 1 + items.length) % items.length;
        break;
      case 'Home':
        next = 0;
        break;
      case 'End':
        next = items.length - 1;
        break;
      default:
        return;
    }

    event.preventDefault();
    this.activeChange.emit(items[next].id);
    this.tabButtons()[next]?.nativeElement.focus();
  }
}
