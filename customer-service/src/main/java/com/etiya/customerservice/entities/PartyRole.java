package com.etiya.customerservice.entities;

import com.etiya.customerservice.entities.reference.PartyRoleType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Party rolü (legacy {@code PARTY_ROLE} karşılığı).
 *
 * <p>Bir {@link Party}'nin oynadığı rolü temsil eder. {@link Customer}, tipi
 * {@code CUST} olan bir party rolüne bağlanır; böylece diyagramdaki
 * {@code CUST -> PARTY_ROLE -> PARTY} zinciri birebir kurulur. İleride aynı
 * party'ye farklı roller (bayi, tedarikçi) eklenebilir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "party_roles")
public class PartyRole extends StatusAwareEntity {

    /** Rolü oynayan taraf (legacy {@code PARTY_ID}). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    /**
     * Rol tipi (legacy {@code PARTY_ROLE_TP_ID}), ör. {@code CUST}.
     *
     * <p>İş yaşam döngüsü durumu (Aktif/Pasif/Silinmiş) {@code entity_code_name = PARTY_ROLE}
     * dilimindeki {@code general_status} satırlarına {@link StatusAwareEntity#getGeneralStatus()}
     * FK'si ile bağlanır.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_role_type_id", nullable = false)
    private PartyRoleType partyRoleType;
}
