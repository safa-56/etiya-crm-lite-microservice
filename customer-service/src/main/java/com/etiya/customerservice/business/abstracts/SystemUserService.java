package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.entities.SystemUser;

import java.util.Optional;

/**
 * Sistem kullanıcısının domain kimliğini yöneten servis.
 *
 * <p>Kullanıcılar bu serviste <b>oluşturulmaz</b>; Keycloak'ta (admin konsolu ya da
 * realm import'u ile) açılırlar. Buradaki tek sorumluluk, geçerli bir JWT ile gelen
 * kullanıcıya domain tarafında bir {@code Party → PartyRole(USER) → SystemUser}
 * zinciri karşılığı olduğundan emin olmaktır (lazy provisioning).
 */
public interface SystemUserService {

    /**
     * Verilen Keycloak kullanıcısı için domain kaydının var olduğundan emin olur;
     * yoksa party zinciriyle birlikte tek transaction'da oluşturur.
     *
     * <p>Idempotenttir ve her istekte çağrılabilecek kadar ucuzdur: bilinen
     * kullanıcılar için veritabanına hiç gitmez.
     *
     * @param keycloakUserId JWT'nin {@code sub} claim'i
     * @param username       JWT'nin {@code preferred_username} claim'i
     */
    void ensureProvisioned(String keycloakUserId, String username);

    /** Keycloak kimliğine karşılık gelen domain kullanıcısını getirir. */
    Optional<SystemUser> findByKeycloakUserId(String keycloakUserId);
}
