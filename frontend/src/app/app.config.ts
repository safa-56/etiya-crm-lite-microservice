import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners
} from '@angular/core';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeTr from '@angular/common/locales/tr';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { AuthService } from './core/auth/auth.service';

/** Ay adlarının Türkçe biçimlenebilmesi için locale verisi kaydedilir. */
registerLocaleData(localeTr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    /**
     * Uygulama açılmadan önce oturumu geri yükler: sekmede geçerli bir refresh
     * token varsa sessizce yeni access token alınır. Router guard'ları ancak bundan
     * sonra çalıştığı için sayfa yenilemede kullanıcı login'e düşmez.
     */
    provideAppInitializer(() => inject(AuthService).restoreSession())
  ]
};
