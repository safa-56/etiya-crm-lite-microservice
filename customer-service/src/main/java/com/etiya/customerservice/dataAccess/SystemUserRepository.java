package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Sistem kullanıcısı ({@code system_users}) veri erişimi.
 */
@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    /** Keycloak kimliğine ({@code sub}) göre kullanıcıyı getirir. */
    Optional<SystemUser> findByKeycloakUserId(String keycloakUserId);

    /** Keycloak kimliği için domain kaydının zaten var olup olmadığını kontrol eder. */
    boolean existsByKeycloakUserId(String keycloakUserId);
}
