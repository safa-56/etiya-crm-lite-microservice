import { Component, computed, input } from '@angular/core';

export type AvatarSize = 'sm' | 'md' | 'lg';

const SIZES: Record<AvatarSize, string> = {
  sm: 'h-8 w-8 text-[11px]',
  md: 'h-9 w-9 text-xs',
  lg: 'h-16 w-16 text-lg'
};

/**
 * Baş harf dairesi. Bilgi taşımadığı (ad zaten yanında yazdığı) için ekran okuyuculardan
 * gizlenir.
 */
@Component({
  selector: 'app-avatar',
  host: { class: 'contents' },
  template: ` <span aria-hidden="true" [class]="classes()">{{ initials() }}</span> `
})
export class Avatar {
  readonly initials = input.required<string>();
  readonly size = input<AvatarSize>('md');
  readonly tone = input<'solid' | 'soft'>('solid');

  protected readonly classes = computed(() => {
    const tone =
      this.tone() === 'solid' ? 'bg-etiya-navy text-white' : 'bg-etiya-navy/10 text-etiya-navy';
    return `flex shrink-0 items-center justify-center rounded-full font-semibold ${SIZES[this.size()]} ${tone}`;
  });
}
