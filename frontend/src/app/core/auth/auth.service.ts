import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, firstValueFrom, map, of, tap, throwError } from 'rxjs';

import {
  KEYCLOAK_CLIENT_ID,
  KEYCLOAK_LOGOUT_URL,
  KEYCLOAK_TOKEN_URL
} from '../config/api.config';
import { AuthStore } from './auth.store';
import { KeycloakErrorResponse, LoginFailureReason, TokenResponse } from './auth.model';

/**
 * Keycloak ile kimlik doğrulama (Resource Owner Password Credentials / "direct grant").
 *
 * <p>Bu akış, uygulamanın <b>kendi giriş ekranını</b> kullanmasına izin verir:
 * kullanıcı adı/parola doğrudan Keycloak'ın token ucuna gönderilir, dönen access
 * token gateway'e `Bearer` olarak iletilir. Kullanıcılar burada oluşturulmaz —
 * Keycloak admin konsolunda (ya da realm import'uyla) açılırlar; parolanın,
 * rollerin ve oturum politikalarının tek otoritesi Keycloak'tır.
 *
 * <p>Realm tarafındaki kurallar (5 hatada 15 dk kilit, 8 saat idle timeout,
 * parola politikası) bu akışta da uygulanır; token ucu ihlalde `invalid_grant` döner.
 */
@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly store = inject(AuthStore);

  /**
   * Kullanıcı adı/parola ile giriş yapar. Başarılıysa oturumu kurar.
   * Hata durumunda istisna fırlatmaz; nedeni döndürür ki ekran mesajı seçebilsin.
   */
  login(username: string, password: string): Observable<LoginFailureReason | null> {
    const body = new HttpParams()
      .set('client_id', KEYCLOAK_CLIENT_ID)
      .set('grant_type', 'password')
      // Kullanıcı adının baş/son boşlukları temizlenir (Keycloak temasındaki
      // davranışın karşılığı); Keycloak kullanıcı adında büyük/küçük harf duyarsızdır.
      .set('username', username.trim())
      .set('password', password);

    return this.requestToken(body).pipe(
      map(() => null),
      catchError((error: HttpErrorResponse) => of(this.toFailureReason(error)))
    );
  }

  /**
   * Refresh token ile yeni bir access token alır. Oturum yenilenemezse temizlenir.
   * Uygulama açılışında ve 401 alındığında çağrılır.
   */
  refresh(): Observable<boolean> {
    const refreshToken = this.store.readRefreshToken();

    if (refreshToken === null) {
      return of(false);
    }

    const body = new HttpParams()
      .set('client_id', KEYCLOAK_CLIENT_ID)
      .set('grant_type', 'refresh_token')
      .set('refresh_token', refreshToken);

    return this.requestToken(body).pipe(
      map(() => true),
      catchError(() => {
        // Refresh token süresi dolmuş ya da oturum sunucuda sonlandırılmış.
        this.store.clear();
        return of(false);
      })
    );
  }

  /**
   * Uygulama açılışında çağrılır: elde refresh token varsa oturumu sessizce geri yükler.
   * Böylece sayfa yenilendiğinde kullanıcı giriş ekranına düşmez.
   */
  restoreSession(): Promise<boolean> {
    if (this.store.readRefreshToken() === null) {
      return Promise.resolve(false);
    }

    return firstValueFrom(this.refresh());
  }

  /**
   * Oturumu kapatır. Yerel durum her hâlükârda temizlenir; Keycloak'taki oturumu da
   * sonlandırmak için logout ucu çağrılır (başarısız olsa bile kullanıcı çıkmış sayılır).
   */
  logout(): Observable<void> {
    const refreshToken = this.store.readRefreshToken();
    this.store.clear();

    if (refreshToken === null) {
      return of(undefined);
    }

    const body = new HttpParams()
      .set('client_id', KEYCLOAK_CLIENT_ID)
      .set('refresh_token', refreshToken);

    return this.http.post<void>(KEYCLOAK_LOGOUT_URL, body).pipe(catchError(() => of(undefined)));
  }

  /** Token ucuna form-encoded istek atar ve başarılı yanıtta oturumu kurar. */
  private requestToken(body: HttpParams): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(KEYCLOAK_TOKEN_URL, body)
      .pipe(
        tap((response) => this.store.setSession(response.access_token, response.refresh_token)),
        catchError((error: HttpErrorResponse) => throwError(() => error))
      );
  }

  /**
   * Keycloak hatasını ekranda gösterilecek nedene çevirir.
   *
   * <p>`invalid_grant`, hatalı parolayı da kilitlenmiş hesabı da kapsar: Keycloak
   * hangisi olduğunu bilinçli olarak açık etmez (kullanıcı adı sızdırmamak için).
   */
  private toFailureReason(error: HttpErrorResponse): LoginFailureReason {
    // status 0: sunucuya hiç ulaşılamadı (Keycloak kapalı ya da ağ/CORS engeli).
    if (error.status === 0) {
      return 'unavailable';
    }

    const body = error.error as KeycloakErrorResponse | null;

    if (body?.error === 'invalid_grant') {
      return 'invalidCredentials';
    }

    return 'unknown';
  }
}
