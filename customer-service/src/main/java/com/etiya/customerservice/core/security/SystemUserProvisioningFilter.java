package com.etiya.customerservice.core.security;

import com.etiya.customerservice.business.abstracts.SystemUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Doğrulanmış her JWT isteğinde, kullanıcının domain kimliğinin var olmasını sağlar
 * (lazy provisioning).
 *
 * <p>Keycloak'ta açılan bir kullanıcı customer-service'in veritabanına hiçbir şey
 * yazmaz; zincir bu filtrede, kullanıcının <b>ilk isteğinde</b> kurulur:
 * <pre>
 *   Party (INDV, ACTV)
 *     └── PartyRole (type=USER, ACTV)
 *           └── SystemUser (keycloak_user_id = JWT 'sub')
 * </pre>
 *
 * <p>Güvenlik zincirinde yetkilendirmeden <b>sonra</b> konumlanır: yalnızca kimliği
 * doğrulanmış ve yetkisi geçmiş istekler kayıt yaratabilir. Kimlik doğrulaması
 * gerektirmeyen uçlarda (actuator, swagger) {@link JwtAuthenticationToken} bulunmadığı
 * için filtre hiçbir şey yapmaz; aynı şekilde Kafka tüketicileri ve hermetik testler
 * de HTTP üzerinden geçmediğinden etkilenmez.
 *
 * <p><b>Hata politikası:</b> provizyon yardımcı bir iştir; başarısız olursa istek
 * düşürülmez, yalnızca loglanır. Aksi hâlde referans verisi eksik bir ortamda tüm API
 * kullanılamaz hâle gelirdi. Kayıt bir sonraki istekte yeniden denenir.
 */
@Component
@Order(SystemUserProvisioningFilter.FILTER_ORDER)
public class SystemUserProvisioningFilter extends OncePerRequestFilter {

    /**
     * Spring Security zincirinin tamamının arkasında kalacak kadar geç bir sıra.
     * Filtre yalnızca {@link SecurityContextHolder} dolduktan sonra anlamlıdır.
     */
    static final int FILTER_ORDER = Integer.MAX_VALUE - 100;

    private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

    private static final Logger logger = LoggerFactory.getLogger(SystemUserProvisioningFilter.class);

    private final SystemUserService systemUserService;

    public SystemUserProvisioningFilter(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        provisionCurrentUser();
        filterChain.doFilter(request, response);
    }

    private void provisionCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return;
        }

        Jwt jwt = jwtAuthentication.getToken();

        try {
            systemUserService.ensureProvisioned(
                    jwt.getSubject(), jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM));
        } catch (Exception exception) {
            logger.warn("Sistem kullanicisi provizyonu basarisiz oldu: sub={}", jwt.getSubject(), exception);
        }
    }
}
