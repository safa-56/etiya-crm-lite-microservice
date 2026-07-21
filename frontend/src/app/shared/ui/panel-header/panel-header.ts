import { Component, computed, input } from '@angular/core';

/**
 * Panel başlığı: turuncu başlık + başlığın yanındaki (`inline`) ve satır sonundaki (`end`)
 * aksiyonlar.
 */
@Component({
  selector: 'app-panel-header',
  host: { '[class]': 'classes()' },
  template: `
    <div class="flex items-center gap-3">
      <h3 class="text-base font-bold text-etiya-orange">{{ heading() }}</h3>
      <ng-content select="[inline]" />
    </div>

    <ng-content select="[end]" />
  `
})
export class PanelHeader {
  readonly heading = input.required<string>();

  /** Başlığı gövdeden ayıran ince çizgi; aksiyon düğmesi olan panellerde kapatılır. */
  readonly divider = input(true);

  protected readonly classes = computed(() => {
    const border = this.divider() ? 'border-b border-slate-100' : '';
    return `flex flex-wrap items-center justify-between gap-3 pb-4 ${border}`;
  });
}
