package com.etiya.orderservice.core.crosscutting.exceptions;

/**
 * Is kurali ihlallerinde firlatilan ozel (custom) exception.
 *
 * <p>Is katmanindaki kural siniflari (rules) ve manager'lar, bir is kisiti
 * saglanmadiginda bunu firlatir. {@code getMessage()} artik dogrudan metin degil,
 * {@code messages*.properties} icindeki bir <b>mesaj anahtari</b> tasir; cozumleme
 * (ve dile gore ceviri) {@code GlobalExceptionHandler} icinde {@code MessageSource}
 * ile yapilir. Parametreli mesajlar icin {@code args} ({@code {0}, {1} ...})
 * gecirilebilir.
 */
public class BusinessException extends RuntimeException {

    /** Mesaj sablonundaki {@code {0}, {1} ...} yer tutucularina karsilik gelen degerler. */
    private final transient Object[] args;

    /** Parametresiz mesaj anahtari ile. */
    public BusinessException(String messageKey) {
        super(messageKey);
        this.args = null;
    }

    /** Parametreli mesaj anahtari ile ({@code {0}, {1} ...}). */
    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    /** Mesaj sablonu parametreleri (yoksa {@code null}). */
    public Object[] getArgs() {
        return args;
    }
}
