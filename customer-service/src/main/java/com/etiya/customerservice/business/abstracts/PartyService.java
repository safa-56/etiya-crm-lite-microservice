package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.entities.Party;

/**
 * Party (taraf) iş servisi.
 */
public interface PartyService {

    /**
     * Bireysel tipte ({@code PARTY/IND}), aktif durumda yeni bir party oluşturur
     * ve persist eder.
     */
    Party createIndividualParty();

    /**
     * Party'yi pasifleştirir: satır soft-delete'i ({@code isActive=false},
     * {@code deletedDate}) uygulanır ve iş durumu {@code PARTY/DEL} yapılır.
     */
    void deactivate(Party party);
}
