package com.etiya.customerservice.business.constants;

/**
 * İstek DTO'larında paylaşılan doğrulama desenleri.
 *
 * <p>Desenler {@code @Pattern} anotasyonunda kullanıldığından <b>derleme zamanı
 * sabiti</b> olmak zorundadır; bu yüzden {@code String} olarak tutulurlar.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    /**
     * Ad/soyad alanları: Türkçe dâhil harfler, boşluk, kesme işareti ve tire.
     * Rakam ve diğer özel karakterler kabul edilmez.
     *
     * <p>Düz kesme ({@code '}) yanında tipografik kesme ({@code ’}) de kabul edilir;
     * bazı klavyeler ve kopyala-yapıştır kaynakları bunu üretir, kullanıcı ikisini
     * ayırt edemez. Şapkalı harfler (â, î, û) Türkçe adlarda geçtiğinden listededir.
     *
     * <p>Nicelik belirteci {@code *}'dır: boş değer bu desenle değil, alanın kendi
     * {@code @NotBlank} kuralıyla ele alınır (opsiyonel adlar boş geçilebilmelidir).
     *
     * <p>Frontend'deki {@code NAME_CHARACTERS} sabiti bu kümenin aynısını tanımlar;
     * biri değişirse diğeri de değişmelidir.
     */
    public static final String NAME_PATTERN = "^[A-Za-zÇçĞğİıÖöŞşÜüÂâÎîÛû '’-]*$";
}
