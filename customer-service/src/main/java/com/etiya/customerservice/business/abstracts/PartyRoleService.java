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
     * Bireysel tipte yeni bir party açıp ona {@code USER} rolü bağlar ve persist eder.
     * Sisteme ilk kez giriş yapan bir Keycloak kullanıcısı için kullanılır.
     */
    PartyRole createUserRoleForIndividual();

    /**
     * Rolü pasifleştirir (soft-delete + iş durumu {@code DEL}). Müşteri silindiğinde
     * zincirin tamamı tutarlı kalsın diye çağrılır.
     *
     * <p>Bağlı party yalnızca <b>son aktif rol</b> de düştüğünde pasifleştirilir: bir
     * party birden çok rol taşıyabildiğinden (ör. aynı kişinin {@code CUST} ve
     * {@code USER} rolleri), müşteri kaydını silmek o kişinin sisteme girişini
     * öldürmemelidir.
     */
    void deactivate(PartyRole partyRole);
}
