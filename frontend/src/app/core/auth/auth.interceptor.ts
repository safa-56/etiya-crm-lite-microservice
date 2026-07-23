import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { AuthService } from './auth.service';
import { AuthStore } from './auth.store';

/**
 * İş API'lerine giden isteklere `Authorization: Bearer <token>` ekler ve süresi
 * dolmuş oturumu bir kez yenilemeyi dener.
 *
 * <p>Yalnızca {@link API_BASE_URL} (gateway) altındaki isteklere dokunur; Keycloak'ın
 * kendi token/logout uçlarına <b>token eklenmez</b> — onlar zaten kimlik doğrulama
 * akışının parçasıdır ve eklenmesi sonsuz döngü yaratırdı.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith(API_BASE_URL)) {
    return next(request);
  }

  const store = inject(AuthStore);
  const auth = inject(AuthService);
  const router = inject(Router);

  const authorized = withBearer(request, store.accessToken());

  return next(authorized).pipe(
    catchError((error: unknown) => {
      const isUnauthorized = error instanceof HttpErrorResponse && error.status === 401;

      if (!isUnauthorized) {
        return throwError(() => error);
      }

      // Access token'ın ömrü 30 dakika; süresi dolduysa refresh token ile
      // sessizce yenileyip isteği BİR kez tekrarlarız.
      return auth.refresh().pipe(
        switchMap((refreshed) => {
          if (!refreshed) {
            void router.navigate(['/login']);
            return throwError(() => error);
          }

          return next(withBearer(request, store.accessToken()));
        })
      );
    })
  );
};

function withBearer<T>(request: HttpRequest<T>, accessToken: string | null): HttpRequest<T> {
  if (accessToken === null) {
    return request;
  }

  return request.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } });
}
