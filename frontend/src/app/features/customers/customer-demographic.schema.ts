import { maxLength, pattern, required, schema, validate } from '@angular/forms/signals';

import { NAME_CHARACTERS } from '../../shared/directives/character-mask';
import { Gender } from './customer.model';

/**
 * Müşterinin demografik alanları. Hem oluşturma sihirbazı (1. adım) hem müşteri
 * detayındaki düzenleme formu <b>aynı</b> alanları taşır; bu yüzden tip ve kurallar
 * burada tek yerde durur.
 */
export interface CustomerDraft {
  firstName: string;
  secondName: string;
  lastName: string;
  birthDate: string;
  gender: Gender;
  fatherName: string;
  motherName: string;
  identityNumber: string;
}

/**
 * Ad alanlarının kabul ettiği tam biçim. Girdi maskesi (`appLettersOnly`) izinsiz
 * karakteri zaten engeller; bu desen, maskeyi atlayan yolları (programatik değer,
 * tarayıcı otomatik doldurma) da yakalayan ikinci katmandır ve backend'deki
 * `ValidationPatterns.NAME_PATTERN` ile aynı kümeyi tanımlar.
 */
const NAME_PATTERN = new RegExp(`^[${NAME_CHARACTERS}]*$`);

/** TC kimlik numarası: tam 11 hane, yalnızca rakam. */
const IDENTITY_NUMBER_PATTERN = /^\d{11}$/;

/**
 * Bugünün tarihini yerel saate göre {@code yyyy-MM-dd} biçiminde döndürür.
 * `type="date"` girişinin `max` özniteliği ve doğum tarihi doğrulaması aynı
 * eşiği kullansın diye tek yerde üretilir. {@code toISOString()} UTC'ye
 * çevirdiğinden gece yarısı civarında bir günlük kaymaya yol açar; bu yüzden
 * bileşen yerel alanlardan kurulur.
 */
export function todayIsoDate(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Demografik alanların doğrulama kuralları.
 *
 * <p>Uzunluk sınırları backend DTO'sundaki {@code @Size} değerleriyle birebir aynıdır;
 * Signal Forms `maxlength` özniteliğini şablondan değil bu şemadan üretir.
 */
export const customerDemographicSchema = schema<CustomerDraft>((draft) => {
  required(draft.firstName);
  maxLength(draft.firstName, 50);
  pattern(draft.firstName, NAME_PATTERN);

  maxLength(draft.secondName, 100);
  pattern(draft.secondName, NAME_PATTERN);

  required(draft.lastName);
  maxLength(draft.lastName, 50);
  pattern(draft.lastName, NAME_PATTERN);

  required(draft.birthDate);
  // Doğum tarihi ileri tarihli olamaz; bugünden sonrası reddedilir.
  validate(draft.birthDate, ({ value }) => {
    const birthDate = value();
    if (birthDate && birthDate > todayIsoDate()) {
      return { kind: 'futureBirthDate' };
    }
    return undefined;
  });
  required(draft.gender);

  maxLength(draft.fatherName, 100);
  pattern(draft.fatherName, NAME_PATTERN);

  maxLength(draft.motherName, 100);
  pattern(draft.motherName, NAME_PATTERN);

  required(draft.identityNumber);
  maxLength(draft.identityNumber, 11);
  pattern(draft.identityNumber, IDENTITY_NUMBER_PATTERN);
});
