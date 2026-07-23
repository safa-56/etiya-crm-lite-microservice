/** Keycloak token ucunun döndürdüğü gövde (ihtiyaç duyulan alanlar). */
export interface TokenResponse {
  readonly access_token: string;
  readonly refresh_token: string;
  readonly expires_in: number;
}

/** Keycloak hata gövdesi: `{ error, error_description }`. */
export interface KeycloakErrorResponse {
  readonly error?: string;
  readonly error_description?: string;
}

/** Girişin neden başarısız olduğu; kullanıcıya gösterilecek mesaja çevrilir. */
export type LoginFailureReason = 'invalidCredentials' | 'unavailable' | 'unknown';

/** Access token içinden okunan, uygulamanın ihtiyaç duyduğu kullanıcı bilgisi. */
export interface AuthenticatedUser {
  /** Keycloak'ın değişmez kullanıcı kimliği (`sub`) — backend'deki SystemUser ile eşleşir. */
  readonly id: string;
  readonly username: string;
  /** Ad soyad (`name`); yoksa kullanıcı adına düşülür. */
  readonly displayName: string;
  /** `realm_access.roles` — yetkiye göre menü/aksiyon göstermek için. */
  readonly roles: readonly string[];
}

/** JWT payload'ının okuduğumuz alanları. */
interface AccessTokenClaims {
  readonly sub?: string;
  readonly preferred_username?: string;
  readonly name?: string;
  readonly realm_access?: { readonly roles?: readonly string[] };
  readonly exp?: number;
}

/**
 * JWT'nin payload bölümünü çözer. İmza **doğrulanmaz** — bunu yapan taraf
 * gateway ve iş servisleridir. Buradaki tek amaç, sunucuya sormadan kullanıcı
 * adını/rollerini ekranda gösterebilmektir.
 */
function decodeClaims(accessToken: string): AccessTokenClaims | null {
  const payload = accessToken.split('.')[1];

  if (payload === undefined) {
    return null;
  }

  try {
    // JWT base64url kullanır: atob için standart base64'e çevrilmeli.
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((character) => `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join('')
    );

    return JSON.parse(json) as AccessTokenClaims;
  } catch {
    return null;
  }
}

/** Access token'dan uygulama kullanıcısını çıkarır; çözülemezse null döner. */
export function readUserFromToken(accessToken: string): AuthenticatedUser | null {
  const claims = decodeClaims(accessToken);

  if (claims?.sub === undefined || claims.preferred_username === undefined) {
    return null;
  }

  return {
    id: claims.sub,
    username: claims.preferred_username,
    displayName: claims.name ?? claims.preferred_username,
    roles: claims.realm_access?.roles ?? []
  };
}

/** Kullanıcı adı/ad soyaddan avatar baş harflerini üretir. */
export function userInitials(user: AuthenticatedUser): string {
  return user.displayName
    .split(' ')
    .filter((part) => part.length > 0)
    .slice(0, 2)
    .map((part) => part.charAt(0).toLocaleUpperCase('tr-TR'))
    .join('');
}
