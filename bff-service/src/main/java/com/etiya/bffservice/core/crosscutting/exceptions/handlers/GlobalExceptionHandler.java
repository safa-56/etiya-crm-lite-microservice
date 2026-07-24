package com.etiya.bffservice.core.crosscutting.exceptions.handlers;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

/**
 * BFF geneli hata çevirisi.
 *
 * <p>Downstream servis bir hata döndürdüğünde ({@link RestClientResponseException}),
 * BFF aynı HTTP durumunu istemciye yansıtır (ör. customer-service 404 → BFF 404).
 * Böylece frontend "bulunamadı"yı 500'den ayırt edebilir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail handleDownstreamError(RestClientResponseException exception) {
        HttpStatusCode status = exception.getStatusCode();
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle("Downstream service error");
        problem.setDetail("Bağlı servis isteği reddetti veya kaynağı bulamadı.");
        return problem;
    }
}
