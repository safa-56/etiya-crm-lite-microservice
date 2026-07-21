import { Component, computed, inject, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { I18nService } from '../../../core/i18n/i18n.service';
import { LanguageSwitcher } from '../../../shared/ui/language-switcher/language-switcher';

@Component({
  selector: 'app-login',
  imports: [NgOptimizedImage, ReactiveFormsModule, LanguageSwitcher],
  templateUrl: './login.html'
})
export class Login {
  private readonly formBuilder = inject(FormBuilder);

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

    // TODO: kimlik doğrulama servisi bağlanacak.
  }
}
