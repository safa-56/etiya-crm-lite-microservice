package com.etiya.accountservice.entities.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * customer-service adresinin account-service'teki yerel <b>projeksiyonu</b> (read-model).
 *
 * <p>Bir müşteriye ({@code customerId}) bağlıdır. Fatura hesabı oluşturulurken
 * seçilen adres ({@code addressId}) bu tabloda müşteriye ait olarak doğrulanır ve
 * hesabın adres metni buradan çözülür (snapshot). Kimlik ({@code addressId})
 * customer-service'ten gelir; üretilmez.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "customer_address_projections",
        indexes = @Index(name = "idx_addr_proj_customer", columnList = "customer_id")
)
public class CustomerAddressProjection {

    /** customer-service'teki adres id'si (üretilmez, olaydan gelir). */
    @Id
    @Column(name = "address_id", nullable = false, updatable = false)
    private Long addressId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "address_description")
    private String addressDescription;

    @Column(name = "is_primary")
    private Boolean isPrimary;
}
