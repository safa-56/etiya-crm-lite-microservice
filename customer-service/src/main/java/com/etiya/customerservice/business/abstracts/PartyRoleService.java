package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.entities.PartyRole;

/**
 * Party rolü iş servisi.
 */
public interface PartyRoleService {

    /**
     * Yeni bir bireysel party ve onun üzerinde {@code CUST} tipli bir rol
     * oluşturur; {@code CUST -> PARTY_ROLE -> PARTY} zincirinin üst iki halkasını
     * hazırlayıp döner. Dönen rol, {@code Customer.partyRole}'a atanmak içindir.
     */
    PartyRole createCustomerRoleForIndividual();

    /**
     * Rolü ve bağlı olduğu party'yi pasifleştirir (soft-delete + iş durumu
     * {@code DEL}). Müşteri silindiğinde zincirin tamamı tutarlı kalsın diye
     * çağrılır.
     */
    void deactivate(PartyRole partyRole);
}
