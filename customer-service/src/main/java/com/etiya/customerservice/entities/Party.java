package com.etiya.customerservice.entities;

import com.etiya.customerservice.entities.reference.GeneralStatus;
import com.etiya.customerservice.entities.reference.GeneralType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Party (taraf) — legacy {@code PARTY} karşılığı.
 *
 * <p>Kişi veya kurum olabilen üst kimliktir; oynadığı roller {@link PartyRole}
 * ile modellenir ({@code 1:N}).
 *
 * <p><b>Modelleme notu:</b> Orijinal SID modelinde birey ({@code IND}) doğrudan
 * Party'ye bağlanır ve {@code CUST} ile kardeştir. Bu projede
 * {@link IndividualCustomer}'ın {@link Customer}'ı miras alan (JOINED) mevcut
 * yapısı korunduğu için birey, Party'ye <i>dolaylı</i> bağlanır:
 * {@code individual_customers -> customers -> party_roles -> parties}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parties")
public class Party extends BaseEntity {

    /**
     * Party tipi (legacy {@code PARTY_TP_ID}): bireysel / kurumsal.
     * {@code entity_code_name = PARTY} dilimindeki {@link GeneralType} satırlarına bağlanır.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_type_id", nullable = false)
    private GeneralType partyType;

    /**
     * İş yaşam döngüsü durumu (legacy {@code ST_ID}); {@code entity_code_name = PARTY}
     * dilimindeki {@link GeneralStatus} satırlarına bağlanır.
     *
     * <p>{@code BaseEntity.isActive} ile karıştırılmamalıdır: {@code isActive} satır
     * seviyesinde soft-delete bayrağı, bu alan ise iş durumudur (Aktif/Pasif/Silinmiş).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private GeneralStatus status;
}
