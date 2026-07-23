import { Directive } from '@angular/core';

import { CharacterMask, NON_NAME_PATTERN } from './character-mask';

/**
 * Metin girdisini ad/soyad karakterleriyle sınırlar: Türkçe dâhil harfler, boşluk,
 * kesme işareti ve tire. Rakam ve diğer özel karakterler girilemez.
 *
 * <p>Kabul edilen kümenin tanımı ve gerekçesi için bkz. `NAME_CHARACTERS`; davranış
 * ayrıntıları için bkz. {@link CharacterMask}.
 */
@Directive({
  selector: 'input[appLettersOnly]',
  host: {
    '(beforeinput)': 'blockDisallowedTyping($event)',
    '(input)': 'sanitize()'
  }
})
export class LettersOnly extends CharacterMask {
  protected readonly disallowed = NON_NAME_PATTERN;
}
