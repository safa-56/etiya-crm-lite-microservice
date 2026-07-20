package com.etiya.orderservice.core.crosscutting.exceptions.handlers;

import com.etiya.orderservice.core.crosscutting.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
 * Merkezi hata yakalama sinifi.
 *
 * <p>Tum controller'larda olusan istisnalari tek noktada yakalar ve tutarli,
 * RFC 7807 uyumlu {@link ProblemDetail} yanitlarina cevirir. Kullaniciya donen
 * tum metinler {@link MessageSource} uzerinden, istegin d/iline
 * ({@code Accept-Language}) gore Turkce/Ingilizce cozulur.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Verilen anahtari, istegin diline gore cozer; bulunamazsa anahtarin kendisini doner. */
    private String resolve(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }

    /** Is kurali ihlalleri -> 400 Bad Request. */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception) {
        String detail = resolve(exception.getMessage(), exception.getArgs());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(resolve("error.business.title"));
        problem.setType(URI.create("https://etiya.com/crm-lite/errors/business"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }

    /** DTO dogrulama hatalari (@Valid) -> 400 Bad Request + alan bazli detay. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, resolve("error.validation.detail"));
        problem.setTitle(resolve("error.validation.title"));
        problem.setType(URI.create("https://etiya.com/crm-lite/errors/validation"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("validationErrors", validationErrors);
        return problem;
    }

    /** Beklenmeyen tum hatalar -> 500 Internal Server Error. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception, WebRequest request) {
        // Beklenmeyen hatalarin kok nedeni istemciye sizdirilmaz ama sunucuda mutlaka loglanir.
        log.error("Beklenmeyen hata: {}", request.getDescription(false), exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, resolve("error.internal.detail"));
        problem.setTitle(resolve("error.internal.title"));
        problem.setType(URI.create("https://etiya.com/crm-lite/errors/internal"));
        problem.setProperty("timestamp", LocalDateTime.now());
        return problem;
    }
}
