/**
 * Backend adresleri tek yerde. Ortam bazlı ayrım gerektiğinde (docker/prod)
 * burası bir `environment` dosyasına ya da runtime config'e taşınır; bileşenler
 * ve servisler bu sabitlerden okuduğu için değişiklik tek noktada kalır.
 */

/** Tüm iş API'lerinin girişi: gateway-server. */
export const API_BASE_URL = 'http://localhost:8080';

/** Keycloak realm bilgileri; token uçlarına buradan türetilir. */
const KEYCLOAK_BASE_URL = 'http://localhost:8180';
const KEYCLOAK_REALM = 'etiya-crm';

/** Frontend'in kullandığı public client (realm import'unda tanımlı). */
export const KEYCLOAK_CLIENT_ID = 'crm-app';

const REALM_URL = `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect`;

/** Access/refresh token üretme ve yenileme ucu. */
export const KEYCLOAK_TOKEN_URL = `${REALM_URL}/token`;

/** Oturumu sunucu tarafında da sonlandıran uç. */
export const KEYCLOAK_LOGOUT_URL = `${REALM_URL}/logout`;
