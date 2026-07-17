package com.etiya.customerservice.entities.reference;

import com.etiya.customerservice.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Party rol tipi referans tablosu (legacy {@code PARTY_ROLE_TP} karşılığı).
 *
 * <p>Bir party'nin oynayabileceği rolleri tanımlar (ör. {@code CUST} = müşteri).
 * {@code PARTY_ROLE} tamamen bu context'e ait olduğundan, tip tablosu da
 * customer-service'e aittir ve {@link GeneralType}'tan ayrı, adanmış bir
 * lookup olarak tutulur.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "party_role_types")
public class PartyRoleType extends BaseEntity {

    /** Görünen ad (legacy {@code NAME}), ör. "Müşteri". */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Açıklama (legacy {@code DESCR}). */
    @Column(name = "description", length = 500)
    private String description;

    /** Stabil iş kodu (legacy {@code SHRT_CODE}), ör. {@code CUST}. */
    @Column(name = "short_code", nullable = false, length = 50, unique = true)
    private String shortCode;
}
