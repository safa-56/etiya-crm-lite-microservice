package com.etiya.cartservice.core.crosscutting.exceptions;

/**
 * İş kuralı ihlallerinde fırlatılan özel (custom) exception.
 *
 * <p>İş katmanındaki kural sınıfları (rules) ve manager'lar, bir iş kısıtı
 * sağlanmadığında bunu fırlatır. Mesajlar magic string değil, {@code Messages}
 * sabitlerinden gelir. {@code GlobalExceptionHandler} tarafından yakalanıp
 * anlamlı bir HTTP yanıtına (RFC 7807 ProblemDetail) çevrilir.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
