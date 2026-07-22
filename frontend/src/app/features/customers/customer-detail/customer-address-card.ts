import { Component, inject, input, output, signal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Icon } from '../../../shared/ui/icon/icon';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { CustomerAddress } from '../customer.model';

/**
 * Tek adres kartı. Üç nokta menüsü Düzenle/Sil işlemlerini yayar; tek adres kaldığında
 * Sil pasifleşir. `showRadio` açıkken kart, birincil adres seçimi için radyo düğmesi gösterir.
 */
@Component({
  selector: 'app-customer-address-card',
  imports: [Icon, IconButton],
  host: { class: 'relative flex min-h-35 flex-col rounded-xl border border-slate-200 p-4' },
  template: `
    <div class="flex items-start justify-between">
      <span class="text-etiya-orange"><app-icon name="pin" size="h-5 w-5" /></span>

      @if (showMenu()) {
        <div class="relative">
        <app-icon-button
          icon="dots"
          tone="ghost"
          [label]="t().customers.detail.addresses.menu + ': ' + address().title"
          (click)="toggleMenu()"
        />

        @if (menuOpen()) {
          <button
            type="button"
            aria-hidden="true"
            tabindex="-1"
            class="fixed inset-0 z-10 cursor-default"
            (click)="closeMenu()"
          ></button>

          <div
            role="menu"
            class="absolute right-0 top-full z-20 mt-1 w-40 overflow-hidden rounded-lg border border-slate-200 bg-white py-1 shadow-lg"
          >
            <button
              type="button"
              role="menuitem"
              (click)="edit.emit(); closeMenu()"
              class="flex w-full items-center gap-2.5 px-3 py-2 text-left text-sm font-medium text-etiya-navy transition hover:bg-etiya-gray focus:outline-none focus-visible:bg-etiya-gray"
            >
              <span class="text-etiya-orange"><app-icon name="pencil" /></span>
              {{ t().customers.detail.addresses.edit }}
            </button>

            <button
              type="button"
              role="menuitem"
              [disabled]="!canDelete()"
              (click)="delete.emit(); closeMenu()"
              class="flex w-full items-center gap-2.5 px-3 py-2 text-left text-sm font-medium text-rose-600 transition hover:bg-rose-50 focus:outline-none focus-visible:bg-rose-50 disabled:cursor-not-allowed disabled:text-slate-300 disabled:hover:bg-transparent"
            >
              <span [class.text-slate-300]="!canDelete()"><app-icon name="trash" /></span>
              {{ t().customers.detail.addresses.delete }}
            </button>
          </div>
        }
        </div>
      }
    </div>

    <p class="mt-3 text-sm font-bold text-etiya-navy">{{ address().title }}</p>
    <p class="mt-1 text-sm text-slate-500">{{ address().detail }}</p>

    @if (showRadio()) {
      <div class="mt-auto flex justify-end pt-3">
        <input
          type="radio"
          name="primaryAddress"
          [value]="address().id"
          [checked]="isPrimary()"
          (change)="primarySelected.emit(address().id)"
          [attr.aria-label]="t().customers.detail.addresses.primary + ': ' + address().title"
          class="h-5 w-5 accent-etiya-orange focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
        />
      </div>
    }
  `
})
export class CustomerAddressCard {
  protected readonly t = inject(I18nService).t;

  readonly address = input.required<CustomerAddress>();
  readonly isPrimary = input(false);
  readonly showRadio = input(false);
  readonly showMenu = input(true);
  /** Tek adres kaldığında Sil pasifleşsin diye dışarıdan verilir. */
  readonly canDelete = input(true);

  readonly primarySelected = output<string>();
  readonly edit = output<void>();
  readonly delete = output<void>();

  protected readonly menuOpen = signal(false);

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
}
