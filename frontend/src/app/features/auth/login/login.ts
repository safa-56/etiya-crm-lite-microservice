import { Component, computed, inject, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Button } from '../../../shared/ui/button/button';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { Icon } from '../../../shared/ui/icon/icon';
import { InputGroup } from '../../../shared/ui/input-group/input-group';
import { LanguageSwitcher } from '../../../shared/ui/language-switcher/language-switcher';
import { LoginBrandPanel } from './login-brand-panel';

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

  protected readonly t = inject(I18nService).t;

  protected readonly form = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  protected readonly passwordVisible = signal(false);

  protected readonly passwordFieldType = computed(() =>
    this.passwordVisible() ? 'text' : 'password'
  );

  protected togglePasswordVisibility(): void {
    this.passwordVisible.update((visible) => !visible);
  }

  protected hidePassword(): void {
    this.passwordVisible.set(false);
  }

  protected submit(): void {
    // Form gönderildiğinde şifreyi tekrar maskele.
    this.passwordVisible.set(false);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // TODO: kimlik doğrulama servisi bağlanacak; şimdilik doğrudan müşteri sayfasına geçilir.
    this.router.navigate(['/customers']);
  }
}
