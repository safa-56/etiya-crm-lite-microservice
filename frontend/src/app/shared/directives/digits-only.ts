import { Directive } from '@angular/core';

import { CharacterMask, NON_DIGIT_PATTERN } from './character-mask';

/**
 * Metin girdisini yalnızca rakam kabul edecek şekilde kısıtlar (TC kimlik no, hesap
 * numarası, sipariş numarası, GSM gibi alanlar).
 *
 * <p>Davranış ayrıntıları için bkz. {@link CharacterMask}. Uzunluk `maxlength` ile verilir.
 */
@Directive({
  selector: 'input[appDigitsOnly]',
  host: {
    inputmode: 'numeric',
    '(beforeinput)': 'blockDisallowedTyping($event)',
    '(input)': 'sanitize()'
  }
})
export class DigitsOnly extends CharacterMask {
  protected readonly disallowed = NON_DIGIT_PATTERN;
}
