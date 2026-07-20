package com.etiya.cartservice.business.abstracts;

import com.etiya.cartservice.entities.reference.GeneralStatus;

/**
 * Referans (lookup) verisi iş servisi.
 *
 * <p>cart-service'in sahip olduğu durum dilimini stabil iş kodları üzerinden çözer.
 * Diğer manager'lar {@code general_status} tablosuna doğrudan repository ile değil,
 * bu servis üzerinden erişir.
 */
public interface ReferenceDataService {

    /**
     * Verilen entity dilimi ve kısa koda ait durumu çözer.
     *
     * @param entityCodeName sahip olunan dilim (bkz. {@code CartReferenceCodes.ENTITY_*})
     * @param shortCode      stabil iş kodu (ör. {@code ACTV})
     */
    GeneralStatus getStatus(String entityCodeName, String shortCode);
}
