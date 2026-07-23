import { Directive, ElementRef, inject } from '@angular/core';

/**
 * Ad/soyad alanlarında kabul edilen karakterler: Türkçe dâhil harfler, boşluk,
 * kesme işareti ve tire. Rakam ve diğer noktalama işaretleri dışarıdadır.
 *
 * <p>Düz kesme (`'`) yanında tipografik kesme (`’`) de kabul edilir; bazı klavyeler ve
 * kopyala-yapıştır kaynakları bunu üretir ve kullanıcı ikisini ayırt edemez.
 * Şapkalı harfler (â, î, û) Türkçe adlarda geçtiğinden listededir.
 */
export const NAME_CHARACTERS = "A-Za-zÇçĞğİıÖöŞşÜüÂâÎîÛû '’-";

/** {@link NAME_CHARACTERS} dışındaki her karakteri yakalar (ayıklama için). */
export const NON_NAME_PATTERN = new RegExp(`[^${NAME_CHARACTERS}]`, 'g');

/** Rakam olmayan her karakteri yakalar. */
export const NON_DIGIT_PATTERN = /\D/g;

/**
 * Bir metin girdisini belirli bir karakter kümesiyle sınırlayan direktiflerin ortak
 * temeli.
 *
 * <p>İki katmanlı çalışır:
 * <ul>
 *   <li><b>Yazarken</b> (`beforeinput`): izin verilmeyen karakterin girişi baştan
 *       engellenir — tuşa basıldığında ekranda hiç görünmez, sonradan silinmez.</li>
 *   <li><b>Yapıştırma / sürükleme / otomatik doldurma</b> (`input`): olay iptal edilmek
 *       yerine değer ayıklanır. "0532 111 22 33" yapıştıran kullanıcı temizlenmiş hâlini
 *       alır, girişin tamamı reddedilmez.</li>
 * </ul>
 *
 * <p>Değer ayıklandığında sentetik bir `input` olayı yayınlanır. Bu, direktifi hem
 * <b>reactive forms</b> hem <b>signal forms</b> ile uyumlu kılar: her ikisi de değeri
 * bu olay üzerinden okur, dolayısıyla direktifin form API'sini tanıması gerekmez.
 *
 * <p>Uzunluk sınırı bu direktifin işi değildir; `maxlength` ile verilir.
 */
@Directive()
export abstract class CharacterMask {
  private readonly elementRef = inject<ElementRef<HTMLInputElement>>(ElementRef);

  /** Sentetik `input` olayı kendi dinleyicimizi yeniden tetiklemesin diye. */
  private syncing = false;

  /** İzin verilmeyen karakterleri yakalayan, global bayraklı desen. */
  protected abstract readonly disallowed: RegExp;

  /** Klavyeden gelen izinsiz karakteri engeller. */
  protected blockDisallowedTyping(event: InputEvent): void {
    // Yalnızca yazma; silme/geri alma/yapıştırma tipleri sanitize()'a bırakılır.
    if (event.inputType !== 'insertText' || event.data === null) {
      return;
    }

    if (this.containsDisallowed(event.data)) {
      event.preventDefault();
    }
  }

  /** Değerde kalan izinsiz karakterleri ayıklar (yapıştırma vb. yollar için). */
  protected sanitize(): void {
    if (this.syncing) {
      return;
    }

    const input = this.elementRef.nativeElement;
    const cleaned = input.value.replace(this.disallowed, '');

    if (cleaned === input.value) {
      return;
    }

    // İmleç, kendinden önce KALAN karakter sayısına taşınır; aksi hâlde metnin
    // ortasına yapıştırma yapıldığında imleç sona sıçrardı.
    const caret = input.selectionStart ?? input.value.length;
    const nextCaret = input.value.slice(0, caret).replace(this.disallowed, '').length;

    input.value = cleaned;
    input.setSelectionRange(nextCaret, nextCaret);

    this.syncing = true;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    this.syncing = false;
  }

  private containsDisallowed(text: string): boolean {
    // Global desenler lastIndex taşıdığı için test() öncesi sıfırlanır.
    this.disallowed.lastIndex = 0;
    return this.disallowed.test(text);
  }
}
