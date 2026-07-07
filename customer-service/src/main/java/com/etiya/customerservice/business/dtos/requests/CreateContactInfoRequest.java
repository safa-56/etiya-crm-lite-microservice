package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Müşteri iletişim bilgisi oluşturma isteği.
 *
 * <p>İki şekilde kullanılır:
 * <ul>
 *   <li>Bireysel müşteri oluşturma isteğinin içinde <b>iç içe</b> — bu durumda
 *       {@code customerId} gönderilmez, ilişki agrega kökü üzerinden kurulur.</li>
 *   <li>{@code CustomerContactInfosController} üzerinden <b>tek başına</b> — bu
 *       durumda {@code customerId} zorunludur (iş kuralı olarak doğrulanır).</li>
 * </ul>
 */
public record CreateContactInfoRequest(

        /** İletişim bilgisinin ait olduğu müşteri; yalnızca standalone oluşturmada gönderilir. */
        Long customerId,

        @Email(message = "Geçerli bir e-posta adresi giriniz.")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String homePhone,

        @Size(min = 11, max = 11)
        String mobilPhone,

        @Size(max = 20)
        String fax
) {
}
