package com.etiya.cartservice.business.dtos.responses;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Sayfalı liste yanıtı.
 *
 * <p>İçerik ile birlikte toplam kayıt/sayfa bilgisini taşır; istemci sayfalama
 * gösterebilir.
 *
 * @param content       geçerli sayfadaki kayıtlar
 * @param pageNumber    0-tabanlı sayfa indeksi
 * @param pageSize      sayfa başına kayıt sayısı
 * @param totalElements toplam kayıt sayısı
 * @param totalPages    toplam sayfa sayısı
 * @param last          son sayfa mı?
 */
public record PagedResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {

    /** Spring Data {@link Page} nesnesinden bir {@link PagedResponse} üretir. */
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
