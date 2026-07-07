package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Size;

/**
 * Adres oluşturma isteği.
 *
 * <p>İki şekilde kullanılır:
 * <ul>
 *   <li>Bireysel müşteri oluşturma isteğinin içinde <b>iç içe</b> — bu durumda
 *       {@code customerId} gönderilmez, ilişki agrega kökü üzerinden kurulur.</li>
 *   <li>{@code AddressesController} üzerinden <b>tek başına</b> — bu durumda
 *       {@code customerId} zorunludur (iş kuralı olarak doğrulanır).</li>
 * </ul>
 */
public record CreateAddressRequest(

        /** Adresin ait olduğu müşteri; yalnızca standalone oluşturmada gönderilir. */
        Long customerId,

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
