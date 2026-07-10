package com.etiya.searchservice.core.crosscutting.exceptions;

/**
 * İş kuralı ihlallerinde fırlatılan özel (custom) exception.
 *
 * <p>Arama parametrelerinin format doğrulaması (rules) bir kısıt sağlanmadığında
 * bunu fırlatır. Mesajlar magic string değil, {@code Messages} sabitlerinden gelir.
 * {@code GlobalExceptionHandler} tarafından yakalanıp anlamlı bir HTTP yanıtına
 * (RFC 7807 ProblemDetail) çevrilir.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
