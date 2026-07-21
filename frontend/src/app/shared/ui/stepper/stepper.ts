import { Component, input } from '@angular/core';

export interface StepperItem {
  readonly index: number;
  readonly label: string;
}

/** Sihirbaz adım göstergesi. */
@Component({
  selector: 'app-stepper',
  template: `
    <ol class="flex flex-wrap items-center gap-4">
      @for (step of steps(); track step.index; let last = $last) {
        <li
          class="flex items-center gap-3"
          [class.flex-1]="!last"
          [attr.aria-current]="active() === step.index ? 'step' : null"
        >
          <span
            aria-hidden="true"
            class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold"
            [class]="
              active() === step.index
                ? 'bg-etiya-orange text-white'
                : 'bg-etiya-gray text-slate-400'
            "
          >
            {{ step.index }}
          </span>
          <span
            class="text-sm whitespace-nowrap"
            [class]="
              active() === step.index ? 'font-bold text-etiya-navy' : 'font-medium text-slate-500'
            "
          >
            {{ step.label }}
          </span>
          @if (!last) {
            <span aria-hidden="true" class="ml-2 hidden h-px flex-1 bg-slate-200 sm:block"></span>
          }
        </li>
      }
    </ol>
  `
})
export class Stepper {
  readonly steps = input.required<readonly StepperItem[]>();
  readonly active = input.required<number>();
}
