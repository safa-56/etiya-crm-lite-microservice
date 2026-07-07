package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Size;

/**
 * Adres güncelleme isteği (tek başına {@code AddressesController} üzerinden).
 *
 * <p>{@code id}, güncellenecek adresi belirtir; controller yol değişkeni (path)
 * ile gövdedeki id'yi tutarlı hale getirir. İlişkili müşteri değiştirilmez.
 */
public record UpdateAddressRequest(

        Long id,

        @Size(max = 100)
        String city,

        @Size(max = 150)
        String street,

        @Size(max = 30)
        String houseNumber,

        @Size(max = 500)
        String addressDescription,

        Boolean isPrimary
) {
}
