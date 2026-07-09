package com.etiya.productservice.core.crosscutting.exceptions.handlers;

import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Merkezi hata yakalama sınıfı.
 *
 * <p>Tüm controller'larda oluşan istisnaları tek noktada yakalar ve tutarlı,
 * RFC 7807 uyumlu {@link ProblemDetail} yanıtlarına çevirir. Böylece hata
 * yönetimi cross-cutting concern olarak merkezileşir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** İş kuralı ihlalleri → 400 Bad Request. */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Business Rule Violation");
        problem.setType(URI.create("https://etiya.com/crm-lite/errors/business"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }

    /** DTO doğrulama hataları (@Valid) → 400 Bad Request + alan bazlı detay. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Doğrulama başarısız. Lütfen gönderilen alanları kontrol edin.");
        problem.setTitle("Validation Error");
        problem.setType(URI.create("https://etiya.com/crm-lite/errors/validation"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("validationErrors", validationErrors);
        return problem;
    }

    /** Beklenmeyen tüm hatalar → 500 Internal Server Error. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception, WebRequest request) {
        // Beklenmeyen hataların kök nedeni istemciye sızdırılmaz ama sunucuda mutlaka loglanır.
        log.error("Beklenmeyen hata: {}", request.getDescription(false), exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu.");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://etiya.com/crm-lite/errors/internal"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }
}
