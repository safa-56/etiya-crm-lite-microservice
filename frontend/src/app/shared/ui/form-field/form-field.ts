import { Component, input } from '@angular/core';

/**
 * Etiket + zorunluluk işareti sarmalayıcısı; kontrolün kendisi içeriye yansıtılır.
 * Etiket ile kontrol arasındaki boşluk buradaki `mb-2` ile verilir, böylece `.field-control`
 * ikonlu/ikonsuz her yerleşimde aynı kalır.
 */
@Component({
  selector: 'app-form-field',
  host: { class: 'block' },
  template: `
    <label [attr.for]="for()" class="mb-2 block text-sm font-medium text-etiya-navy">
      {{ label() }}
      @if (required()) {
        <span aria-hidden="true" class="text-rose-500">*</span>
      }
    </label>

    <ng-content />
  `
})
export class FormFieldShell {
  readonly label = input.required<string>();
  readonly for = input.required<string>();
  readonly required = input(false);
}
