import { Component, input } from '@angular/core';

/**
 * `<dl>` içindeki etiket/değer çifti. Seçici `div[appDetailField]` olduğu için üretilen
 * DOM `<dl> > <div> > <dt>/<dd>` kalır ve tanım listesi kuralları bozulmaz.
 */
@Component({
  selector: 'div[appDetailField]',
  host: { class: 'flex items-baseline gap-4' },
  template: `
    <dt class="w-36 shrink-0 text-sm text-slate-500">{{ label() }}</dt>
    <dd class="text-sm font-semibold text-etiya-navy" [class.break-all]="breakAll()">
      {{ value() }}
    </dd>
  `
})
export class DetailField {
  readonly label = input.required<string>();
  readonly value = input.required<string>();

  /** E-posta gibi uzun değerlerin dar sütunda taşmasını engeller. */
  readonly breakAll = input(false);
}
