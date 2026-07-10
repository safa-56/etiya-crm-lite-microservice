package com.etiya.orderservice.apiController;

import com.etiya.orderservice.business.abstracts.OrderService;
import com.etiya.orderservice.business.dtos.requests.SubmitOrderRequest;
import com.etiya.orderservice.business.dtos.responses.OrderResponse;
import com.etiya.orderservice.business.dtos.responses.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sipariş (Order) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link OrderService} soyutlamasına delege eder; iş kuralları/hata
 * yönetimi/cacheleme alt katmanlarda ele alınır. Sipariş oluşturma (Submit Order —
 * FR-016) bir <b>choreography Saga</b> ile <b>asenkron</b> kesinleşir: uç hemen döner
 * ve sipariş kısa süre {@code PENDING} görünür; cart-service doğrulaması gelince
 * CONFIRMED (ya da telafi ile CANCELLED) olur.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Bir sepeti onaylayıp sipariş oluşturur (Submit Order — asenkron saga ile tamamlanır). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse submit(@Valid @RequestBody SubmitOrderRequest request) {
        return orderService.submit(request);
    }

    /** Id ile tek bir siparişi (satırları ve toplamıyla) getirir. */
    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    /** Aktif siparişleri sayfalı listeler. */
    @GetMapping
    public PagedResponse<OrderResponse> getAll(Pageable pageable) {
        return orderService.getAll(pageable);
    }

    /** Bir müşteriye ait tüm aktif siparişleri getirir. */
    @GetMapping("/customer/{customerId}")
    public List<OrderResponse> getByCustomer(@PathVariable Long customerId) {
        return orderService.getByCustomer(customerId);
    }

    /** Siparişi iptal eder/siler (soft-delete). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
