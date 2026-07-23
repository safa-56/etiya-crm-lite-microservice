import { computed, inject, PLATFORM_ID, Service, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

import { AuthenticatedUser, readUserFromToken } from './auth.model';

/**
 * Sayfa yenilendiğinde oturumun kaybolmaması için refresh token'ın saklandığı anahtar.
 * `sessionStorage` kullanılır: sekme kapanınca oturum düşer, `localStorage`'a göre
 * daha dar bir pencere sunar.
 */
const REFRESH_TOKEN_KEY = 'etiya.refreshToken';

/**
 * Oturum durumunun tek kaynağı.
 *
 * <p><b>Access token bellekte tutulur</b> (signal), diske yazılmaz — XSS ile
 * çalınabileceği en kısa pencere budur. Yalnızca <b>refresh token</b>
 * `sessionStorage`'a yazılır ki sayfa yenilendiğinde kullanıcı tekrar giriş
 * yapmak zorunda kalmasın; açılışta bu token ile sessizce yeni access token alınır
 * (bkz. `AuthService.restoreSession`).
 */
@Service()
export class AuthStore {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly accessTokenSignal = signal<string | null>(null);
  private readonly userSignal = signal<AuthenticatedUser | null>(null);

  /** İstek başlıklarına eklenecek access token. */
  readonly accessToken = this.accessTokenSignal.asReadonly();

  /** Oturum açmış kullanıcı; oturum yoksa null. */
  readonly user = this.userSignal.asReadonly();

  readonly isAuthenticated = computed(() => this.accessTokenSignal() !== null);

  /** Rol bazlı gösterim/aksiyon kontrolü için. */
  hasRole(role: string): boolean {
    return this.userSignal()?.roles.includes(role) ?? false;
  }

  /** Başarılı giriş/yenileme sonrası oturumu kurar. */
  setSession(accessToken: string, refreshToken: string): void {
    this.accessTokenSignal.set(accessToken);
    this.userSignal.set(readUserFromToken(accessToken));
    this.writeRefreshToken(refreshToken);
  }

  /** Oturumu tamamen temizler (çıkış, 401, yenileme başarısız). */
  clear(): void {
    this.accessTokenSignal.set(null);
    this.userSignal.set(null);
    this.writeRefreshToken(null);
  }

  readRefreshToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }

    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private writeRefreshToken(refreshToken: string | null): void {
    if (!this.isBrowser) {
      return;
    }

    if (refreshToken === null) {
      sessionStorage.removeItem(REFRESH_TOKEN_KEY);
      return;
    }

    sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
}
