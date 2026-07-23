import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { KEYCLOAK_LOGOUT_URL, KEYCLOAK_TOKEN_URL } from '../config/api.config';
import { AuthService } from './auth.service';
import { AuthStore } from './auth.store';

/**
 * `sub`, `preferred_username`, `name` ve `realm_access.roles` taşıyan, imzası önemsiz
 * bir access token üretir (istemci imzayı doğrulamaz, yalnızca payload'ı okur).
 */
function fakeAccessToken(): string {
  const payload = {
    sub: 'c717cce4-5c7c-4dc8-9f11-3624fbfb466d',
    preferred_username: 'demo',
    name: 'Demo Kullanici',
    realm_access: { roles: ['crm_user'] }
  };

  return `header.${btoa(JSON.stringify(payload))}.signature`;
}

describe('AuthService', () => {
  let auth: AuthService;
  let store: AuthStore;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    auth = TestBed.inject(AuthService);
    store = TestBed.inject(AuthStore);
    http = TestBed.inject(HttpTestingController);
    sessionStorage.clear();
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  /** Başarılı bir giriş yapıp oturumu kurar (çıkış testlerinin ön koşulu). */
  function login(): void {
    auth.login('demo', 'Password123').subscribe();

    http.expectOne(KEYCLOAK_TOKEN_URL).flush({
      access_token: fakeAccessToken(),
      refresh_token: 'refresh-token-123',
      expires_in: 1800
    });
  }

  it('girişte token ve kullanıcı bilgisini saklar', () => {
    login();

    expect(store.isAuthenticated()).toBe(true);
    expect(store.accessToken()).not.toBeNull();
    expect(store.user()?.username).toBe('demo');
    expect(store.user()?.roles).toContain('crm_user');
    expect(store.readRefreshToken()).toBe('refresh-token-123');
  });

  it('çıkışta oturumu ve token bilgilerini tamamen temizler', () => {
    login();

    auth.logout().subscribe();
    http.expectOne(KEYCLOAK_LOGOUT_URL).flush(null);

    expect(store.isAuthenticated()).toBe(false);
    expect(store.accessToken()).toBeNull();
    expect(store.user()).toBeNull();
    // Sayfa yenilendiğinde oturum geri gelmemeli.
    expect(store.readRefreshToken()).toBeNull();
    expect(sessionStorage.getItem('etiya.refreshToken')).toBeNull();
  });

  it('çıkışta Keycloak oturumunu da sonlandırır', () => {
    login();

    auth.logout().subscribe();

    const request = http.expectOne(KEYCLOAK_LOGOUT_URL);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.toString()).toContain('refresh_token=refresh-token-123');
    request.flush(null);
  });

  it('Keycloak logout ucu hata verse de yerel oturum temizlenir', () => {
    login();

    auth.logout().subscribe();
    http.expectOne(KEYCLOAK_LOGOUT_URL).flush('sunucu hatası', {
      status: 500,
      statusText: 'Server Error'
    });

    expect(store.isAuthenticated()).toBe(false);
    expect(store.readRefreshToken()).toBeNull();
  });

  it('oturum yokken çıkış ağ isteği yapmaz', () => {
    auth.logout().subscribe();

    // http.verify() afterEach'te bekleyen istek olmadığını doğrular.
    expect(store.isAuthenticated()).toBe(false);
  });

  it('çıkış sonrası oturum geri yüklenemez', async () => {
    login();

    auth.logout().subscribe();
    http.expectOne(KEYCLOAK_LOGOUT_URL).flush(null);

    // Refresh token silindiği için restoreSession ağa hiç çıkmaz ve false döner.
    await expect(auth.restoreSession()).resolves.toBe(false);
    expect(store.isAuthenticated()).toBe(false);
  });
});
