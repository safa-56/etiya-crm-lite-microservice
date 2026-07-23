package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.SystemUserService;
import com.etiya.customerservice.dataAccess.SystemUserRepository;
import com.etiya.customerservice.entities.SystemUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistem kullanıcısı iş kuralları — lazy provisioning'in giriş noktası.
 *
 * <p>Bu metot <b>her istekte</b> çağrıldığı için okuma yolu kasıtlı olarak ucuzdur:
 * <ol>
 *   <li>JVM içi küçük bir küme (bu instance'ta daha önce görülmüş {@code sub}'lar)
 *       bilinen kullanıcıları veritabanına hiç gitmeden eler.</li>
 *   <li>Küme ıskalarsa tek bir indeksli {@code exists} sorgusu yapılır.</li>
 *   <li>Kayıt gerçekten yoksa {@link SystemUserProvisioner} ile tek transaction'da
 *       zincir oluşturulur.</li>
 * </ol>
 *
 * <p>Küme yalnızca bir hızlandırmadır; kullanıcı sayısıyla sınırlı büyür ve yeniden
 * başlatmada boşalır — kaybı en fazla kullanıcı başına bir ekstra {@code exists}
 * sorgusudur. Doğruluk kümeye değil, {@code keycloak_user_id} üzerindeki unique
 * kısıta dayanır.
 */
@Service
public class SystemUserManager implements SystemUserService {

    private static final Logger logger = LoggerFactory.getLogger(SystemUserManager.class);

    private final SystemUserRepository systemUserRepository;
    private final SystemUserProvisioner systemUserProvisioner;

    /** Bu instance'ta provizyonu doğrulanmış Keycloak kimlikleri (yalnızca hızlandırma). */
    private final Set<String> provisionedSubjects = ConcurrentHashMap.newKeySet();

    public SystemUserManager(SystemUserRepository systemUserRepository,
                             SystemUserProvisioner systemUserProvisioner) {
        this.systemUserRepository = systemUserRepository;
        this.systemUserProvisioner = systemUserProvisioner;
    }

    @Override
    public void ensureProvisioned(String keycloakUserId, String username) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            return;
        }

        if (provisionedSubjects.contains(keycloakUserId)) {
            return;
        }

        if (systemUserRepository.existsByKeycloakUserId(keycloakUserId)) {
            provisionedSubjects.add(keycloakUserId);
            return;
        }

        try {
            systemUserProvisioner.provision(keycloakUserId, resolveUsername(keycloakUserId, username));
            logger.info("Sistem kullanicisi provizyone edildi: sub={}, username={}", keycloakUserId, username);
        } catch (DataIntegrityViolationException exception) {
            // Aynı kullanıcının eşzamanlı iki ilk isteği yarışabilir; unique kısıt
            // ikinciyi reddeder. Kayıt yine de oluşmuş olduğundan bu bir hata değildir.
            logger.debug("Sistem kullanicisi zaten provizyone edilmis (yaris): sub={}", keycloakUserId);
        }

        provisionedSubjects.add(keycloakUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SystemUser> findByKeycloakUserId(String keycloakUserId) {
        return systemUserRepository.findByKeycloakUserId(keycloakUserId);
    }

    /**
     * {@code preferred_username} claim'i beklenmedik şekilde boş gelirse kolon
     * {@code NOT NULL} olduğu için kayıt yazılamaz; bu durumda {@code sub}'a düşülür.
     */
    private String resolveUsername(String keycloakUserId, String username) {
        return (username == null || username.isBlank()) ? keycloakUserId : username;
    }
}
