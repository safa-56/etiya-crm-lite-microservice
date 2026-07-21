export type ButtonVariant = 'primary' | 'secondary' | 'outline';
export type ButtonSize = 'sm' | 'md' | 'lg' | 'xl';

export const FOCUS_RING =
  'focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2';

const BASE = `inline-flex items-center justify-center gap-2 rounded-lg text-sm font-semibold transition ${FOCUS_RING}`;

const SIZES: Record<ButtonSize, string> = {
  sm: 'px-4 py-2',
  md: 'px-4 py-2.5',
  lg: 'px-5 py-2.5',
  xl: 'px-5 py-3'
};

/**
 * `:enabled` / `:disabled` sözde sınıfları yalnızca form elemanlarında çalışır; bağlantılar
 * için ayrı bir sözlük tutulur.
 */
const BUTTON_VARIANTS: Record<ButtonVariant, string> = {
  primary:
    'text-white enabled:bg-etiya-orange enabled:shadow-sm enabled:hover:bg-etiya-orange-dark disabled:cursor-not-allowed disabled:bg-slate-300 disabled:text-white/80',
  secondary: 'border border-slate-200 bg-white text-etiya-navy hover:bg-etiya-gray',
  outline: 'border border-etiya-navy bg-white text-etiya-navy hover:bg-etiya-gray'
};

const LINK_VARIANTS: Record<ButtonVariant, string> = {
  primary: 'bg-etiya-orange text-white shadow-sm hover:bg-etiya-orange-dark',
  secondary: BUTTON_VARIANTS.secondary,
  outline: BUTTON_VARIANTS.outline
};

export function buttonClass(
  variant: ButtonVariant,
  size: ButtonSize,
  isLink = false,
  full = false
): string {
  const variants = isLink ? LINK_VARIANTS : BUTTON_VARIANTS;
  return `${BASE} ${SIZES[size]} ${variants[variant]} ${full ? 'w-full' : ''}`;
}
