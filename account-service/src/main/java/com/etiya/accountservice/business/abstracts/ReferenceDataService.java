package com.etiya.accountservice.business.abstracts;

import com.etiya.accountservice.entities.reference.GeneralStatus;
import com.etiya.accountservice.entities.reference.GeneralType;

/**
 * Referans (lookup) verisi iş servisi.
 *
 * <p>Bu servis, account-service'in sahip olduğu referans dilimini stabil iş
 * kodları üzerinden çözer. Diğer manager'lar referans tablolarına doğrudan
 * repository ile değil, bu servis üzerinden erişir.
 */
public interface ReferenceDataService {

    /**
     * Verilen entity dilimi ve kısa koda ait aktif tipi çözer.
     *
     * @param entityCodeName sahip olunan dilim (bkz. {@code AccountReferenceCodes.ENTITY_*})
     * @param shortCode      stabil iş kodu
     */
    GeneralType getType(String entityCodeName, String shortCode);

    /**
     * Verilen entity dilimi ve kısa koda ait aktif durumu çözer.
     *
     * @param entityCodeName sahip olunan dilim (bkz. {@code AccountReferenceCodes.ENTITY_*})
     * @param shortCode      stabil iş kodu (ör. {@code ACTV})
     */
    GeneralStatus getStatus(String entityCodeName, String shortCode);
}
