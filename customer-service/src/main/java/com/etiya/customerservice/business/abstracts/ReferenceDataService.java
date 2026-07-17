package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.entities.reference.GeneralStatus;
import com.etiya.customerservice.entities.reference.GeneralType;
import com.etiya.customerservice.entities.reference.PartyRoleType;

/**
 * Referans (lookup) verisi iş servisi.
 *
 * <p>Bu servis, customer-service'in sahip olduğu referans dilimini stabil iş
 * kodları üzerinden çözer. Diğer manager'lar referans tablolarına doğrudan
 * repository ile değil, bu servis üzerinden erişir.
 */
public interface ReferenceDataService {

    /**
     * Verilen entity dilimi ve kısa koda ait aktif tipi çözer.
     *
     * @param entityCodeName sahip olunan dilim (bkz. {@code PartyReferenceCodes.ENTITY_*})
     * @param shortCode      stabil iş kodu (ör. {@code IND})
     */
    GeneralType getType(String entityCodeName, String shortCode);

    /**
     * Verilen entity dilimi ve kısa koda ait aktif durumu çözer.
     *
     * @param entityCodeName sahip olunan dilim (bkz. {@code PartyReferenceCodes.ENTITY_*})
     * @param shortCode      stabil iş kodu (ör. {@code ACTV})
     */
    GeneralStatus getStatus(String entityCodeName, String shortCode);

    /** Verilen kısa koda ait aktif party rol tipini çözer (ör. {@code CUST}). */
    PartyRoleType getPartyRoleType(String shortCode);
}
