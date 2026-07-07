package com.etiya.customerservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Müşteri iletişim bilgileri (CustomerContactInfo).
 *
 * <p>ER modelindeki {@code CustomerContactInfo} tablosuna karşılık gelir;
 * {@link Customer} ile N-1 ilişkilidir ({@code customer_id} FK).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_contact_info")
public class CustomerContactInfo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "home_phone", length = 20)
    private String homePhone;

    @Column(name = "mobil_phone", nullable = false, length = 20)
    private String mobilPhone;

    @Column(name = "fax", length = 20)
    private String fax;
}
