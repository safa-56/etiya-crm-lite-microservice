package com.etiya.customerservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sisteme giriş yapan kullanıcının domain kimliği.
 *
 * <p>Bu kayıt Keycloak kullanıcısının <b>kopyası değildir</b>: parola, roller ve
 * oturum bilgisinin tek otoritesi Keycloak'tır. Burada yalnızca Keycloak'taki
 * kullanıcıya ({@code keycloakUserId} = JWT'nin {@code sub} claim'i) domain
 * tarafında bir kimlik verilir; böylece kullanıcı {@link Party} zincirine bağlanır
 * ve ileride audit alanları gerçek bir FK'ye oturabilir.
 *
 * <p>{@link Customer} ile birebir aynı deseni izler: her ikisi de bir
 * {@link PartyRole}'e 1-1 bağlanır. Fark yalnızca rol tipindedir
 * ({@code CUST} vs. {@code USER}). Aynı {@link Party} her iki rolü de taşıyabilir —
 * "hem müşterimiz hem sistem kullanıcımız olan kişi" bu şekilde modellenir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "system_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_system_users_keycloak_user_id",
                columnNames = "keycloak_user_id"
        )
)
public class SystemUser extends StatusAwareEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_role_id", nullable = false, unique = true)
    private PartyRole partyRole;

    /**
     * Keycloak kullanıcısının değişmez kimliği (JWT {@code sub} claim'i, UUID).
     * Eşleştirme daima bu alan üzerinden yapılır — kullanıcı adı değişebilir,
     * {@code sub} değişmez.
     */
    @Column(name = "keycloak_user_id", nullable = false, length = 64)
    private String keycloakUserId;

    /**
     * Görüntüleme amaçlı kullanıcı adı ({@code preferred_username}). Otoritesi
     * Keycloak'tadır; burada yalnızca raporlama/log kolaylığı için tutulur.
     */
    @Column(name = "username", nullable = false, length = 150)
    private String username;
}
