package com.etiya.orderservice.business.abstracts;

import com.etiya.orderservice.business.dtos.requests.SubmitOrderRequest;
import com.etiya.orderservice.business.dtos.responses.OrderResponse;
import com.etiya.orderservice.business.dtos.responses.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Sipariş (Order) iş servisi soyutlaması.
 *
 * <p>Sepetten siparişe geçişi (checkout — FR-016) ve sipariş görüntüleme/iptal
 * işlemlerini sunar. {@link #submit} çağrısı bir <b>Saga</b> başlatır: sipariş PENDING
 * açılır ve cart-service doğrulamasıyla asenkron olarak kesinleşir.
 */
public interface OrderService {

    /** Bir sepeti onaylayıp sipariş oluşturur (Submit Order — saga ile asenkron kesinleşir). */
    OrderResponse submit(SubmitOrderRequest request);

    /** Id ile tek bir siparişi (satırları ve toplamıyla) getirir. */
    OrderResponse getById(Long id);

    /** Aktif siparişleri sayfalı listeler. */
    PagedResponse<OrderResponse> getAll(Pageable pageable);

    /** Bir müşteriye ait tüm aktif siparişleri getirir. */
    List<OrderResponse> getByCustomer(Long customerId);

    /** Siparişi iptal eder/siler (soft-delete). */
    void delete(Long id);
}
