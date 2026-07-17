package com.etiya.customerservice.entities;

import com.etiya.customerservice.entities.reference.GeneralStatus;
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
public class PartyRole extends BaseEntity {

    /** Rolü oynayan taraf (legacy {@code PARTY_ID}). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    /** Rol tipi (legacy {@code PARTY_ROLE_TP_ID}), ör. {@code CUST}. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_role_type_id", nullable = false)
    private PartyRoleType partyRoleType;

    /**
     * İş yaşam döngüsü durumu (legacy {@code ST_ID}); {@code entity_code_name = PARTY_ROLE}
     * dilimindeki {@link GeneralStatus} satırlarına bağlanır.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private GeneralStatus status;
}
