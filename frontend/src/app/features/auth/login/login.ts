import { Component, computed, inject, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { LoginFailureReason } from '../../../core/auth/auth.model';
import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { InputGroup } from '../../../shared/ui/input-group/input-group';
import { LanguageSwitcher } from '../../../shared/ui/language-switcher/language-switcher';
import { LoginBrandPanel } from './login-brand-panel';

/**
 * Keycloak'ın kullanıcı adı/parola politikasıyla hizalı sınır (realm: `maxLength(50)`).
 * Sunucu tarafı zaten reddeder; buradaki `maxlength` fazla karakterin fiziksel olarak
 * girilmesini engeller — Keycloak'ın kendi login temasındaki davranışın karşılığıdır.
 */
export const CREDENTIAL_MAX_LENGTH = 50;

@Component({
  selector: 'app-login',
  imports: [
    NgOptimizedImage,
    ReactiveFormsModule,
    Button,
    FormFieldShell,
    Icon,
    InputGroup,
    LanguageSwitcher,
    LoginBrandPanel
  ],
  templateUrl: './login.html'
})
export class Login {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

  protected readonly t = inject(I18nService).t;

  protected readonly maxLength = CREDENTIAL_MAX_LENGTH;

  protected readonly form = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(CREDENTIAL_MAX_LENGTH)]],
    password: ['', [Validators.required, Validators.maxLength(CREDENTIAL_MAX_LENGTH)]]
  });

  protected readonly passwordVisible = signal(false);

  /** İstek sürerken formu kilitler ve düğmede ilerleme gösterir. */
  protected readonly submitting = signal(false);

  /** Başarısız girişin nedeni; yeni denemede temizlenir. */
  protected readonly failure = signal<LoginFailureReason | null>(null);

  protected readonly passwordFieldType = computed(() =>
    this.passwordVisible() ? 'text' : 'password'
  );

  /** Hata kutusunda gösterilecek metin; hata yoksa null. */
  protected readonly errorMessage = computed(() => {
    const reason = this.failure();
    return reason === null ? null : this.t().login.errors[reason];
  });

  protected togglePasswordVisibility(): void {
    this.passwordVisible.update((visible) => !visible);
  }

  protected hidePassword(): void {
    this.passwordVisible.set(false);
  }

  protected submit(): void {
    // Form gönderildiğinde şifreyi tekrar maskele.
    this.passwordVisible.set(false);
    this.failure.set(null);

    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();
    this.submitting.set(true);

    this.auth.login(username, password).subscribe((reason) => {
      this.submitting.set(false);

      if (reason !== null) {
        this.failure.set(reason);
        // Başarısız denemeden sonra parola temizlenir, kullanıcı adı korunur.
        this.form.controls.password.reset();
        return;
      }

      void this.router.navigateByUrl(this.redirectTarget());
    });
  }

  /**
   * Guard tarafından login'e yönlendirilen kullanıcı, giriş sonrası gitmek istediği
   * sayfaya döner; doğrudan login'e gelmişse müşteri aramasına düşer.
   */
  private redirectTarget(): string {
    return this.route.snapshot.queryParamMap.get('redirectTo') ?? '/customers';
  }
}
