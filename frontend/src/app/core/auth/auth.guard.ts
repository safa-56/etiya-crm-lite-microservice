import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthStore } from './auth.store';

/**
 * Korumalı sayfalara yalnızca oturum açmış kullanıcıyı alır.
 *
 * <p>Açılışta oturum `provideAppInitializer` ile geri yüklendiğinden (bkz.
 * `app.config.ts`), guard çalıştığında durum kesinleşmiş olur; sayfa yenilemede
 * kullanıcı yanlışlıkla login'e düşmez.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const store = inject(AuthStore);
  const router = inject(Router);

  if (store.isAuthenticated()) {
    return true;
  }

  // Girişten sonra kullanıcıyı gitmek istediği sayfaya döndürebilmek için hedefi taşırız.
  return router.createUrlTree(['/login'], { queryParams: { redirectTo: state.url } });
};

/**
 * Oturumu açık olan kullanıcıyı login sayfasında tutmaz; doğrudan uygulamaya alır.
 */
export const guestGuard: CanActivateFn = () => {
  const store = inject(AuthStore);
  const router = inject(Router);

  return store.isAuthenticated() ? router.createUrlTree(['/customers']) : true;
};
