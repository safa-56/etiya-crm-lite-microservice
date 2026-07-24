import { Component, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FormField, form } from '@angular/forms/signals';

import { I18nService } from '../../../core/i18n/i18n.service';
import { DigitsOnly } from '../../../shared/directives/digits-only';
import { LettersOnly } from '../../../shared/directives/letters-only';
import { Button } from '../../../shared/ui/button/button';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { DetailField } from '../../../shared/ui/detail-field/detail-field';
import { FormFieldShell } from '../../../shared/ui/form-field/form-field';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { Customer } from '../customer.model';
import { CustomerDraft, customerDemographicSchema } from '../customer-demographic.schema';
import { CustomerService } from '../customer.service';

function toDraft(customer: Customer): CustomerDraft {
  return {
    firstName: customer.firstName,
    // Backend'de opsiyonel olan alanlar null gelebilir; form ve trim güvenliği için '' yapılır.
    secondName: customer.secondName ?? '',
    lastName: customer.lastName,
    birthDate: customer.birthDate,
    gender: customer.gender,
    fatherName: customer.fatherName ?? '',
    motherName: customer.motherName ?? '',
    identityNumber: customer.identityNumber
  };
}


/**
 * "Müşteri Bilgisi" sekmesi. Varsayılan olarak salt okunur listeyi gösterir; kalem düğmesine
 * basıldığında aynı alanları düzenlenebilir bir forma çevirir.
 */
@Component({
  selector: 'app-customer-info-panel',
  imports: [
    DatePipe,
    FormField,
    Button,
    ConfirmDialog,
    DetailField,
    FormFieldShell,
    IconButton,
    PanelHeader,
    DigitsOnly,
    LettersOnly
  ],
  host: {
    role: 'tabpanel',
    id: 'panel-info',
    'aria-labelledby': 'tab-info',
    tabindex: '0',
    class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm'
  },
  template: `
    <app-panel-header [heading]="t().customers.detail.info.title">
      <ng-container inline>
        @if (!isEditing()) {
          <app-icon-button
            icon="pencil"
            tone="edit"
            [label]="t().customers.detail.info.edit"
            (click)="startEdit()"
          />
          <app-icon-button
            icon="trash"
            tone="danger"
            [label]="t().customers.detail.info.delete"
            (click)="requestDelete()"
          />
        }
      </ng-container>
    </app-panel-header>

    @if (deleteBlocked()) {
      <div
        role="alert"
        class="mt-4 flex items-start justify-between gap-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
      >
        <span>{{ t().customers.detail.info.deleteBlocked }}</span>
        <button
          type="button"
          class="shrink-0 font-semibold text-amber-900 underline hover:no-underline focus:outline-none focus-visible:ring-2 focus-visible:ring-etiya-orange focus-visible:ring-offset-2"
          (click)="deleteBlocked.set(false)"
        >
          {{ t().customers.detail.info.confirmYes }}
        </button>
      </div>
    }

    @if (deleteError()) {
      <p class="mt-4 text-sm font-medium text-red-600" role="alert">
        {{ t().customers.detail.info.deleteError }}
      </p>
    }

    @if (confirmingDelete()) {
      <app-confirm-dialog
        [message]="t().customers.detail.info.deleteConfirm"
        [confirmLabel]="t().customers.detail.info.confirmYes"
        [cancelLabel]="t().customers.detail.info.confirmNo"
        (confirmed)="deleteCustomer()"
        (cancelled)="confirmingDelete.set(false)"
      />
    }

    @if (isEditing()) {
      <form (submit)="save($event)">
        <div class="mt-5 grid gap-x-12 gap-y-5 sm:grid-cols-2">
          <app-form-field
            for="info-firstName"
            [label]="t().customers.detail.info.firstName"
            [required]="true"
          >
            <input
              id="info-firstName"
              type="text"
              appLettersOnly
              class="field-control"
              [formField]="infoForm.firstName"
              aria-required="true"
            />
          </app-form-field>

          <app-form-field for="info-secondName" [label]="t().customers.detail.info.secondName">
            <input
              id="info-secondName"
              type="text"
              appLettersOnly
              class="field-control"
              [formField]="infoForm.secondName"
            />
          </app-form-field>

          <app-form-field
            for="info-lastName"
            [label]="t().customers.detail.info.lastName"
            [required]="true"
          >
            <input
              id="info-lastName"
              type="text"
              appLettersOnly
              class="field-control"
              [formField]="infoForm.lastName"
              aria-required="true"
            />
          </app-form-field>

          <app-form-field
            for="info-birthDate"
            [label]="t().customers.detail.info.birthDate"
            [required]="true"
          >
            <input
              id="info-birthDate"
              type="date"
              class="field-control"
              [formField]="infoForm.birthDate"
              aria-required="true"
            />
          </app-form-field>

          <app-form-field
            for="info-gender"
            [label]="t().customers.detail.info.gender"
            [required]="true"
          >
            <select
              id="info-gender"
              class="field-control"
              [formField]="infoForm.gender"
              aria-required="true"
            >
              <option value="male">{{ t().customers.gender.male }}</option>
              <option value="female">{{ t().customers.gender.female }}</option>
            </select>
          </app-form-field>

          <app-form-field for="info-fatherName" [label]="t().customers.detail.info.fatherName">
            <input
              id="info-fatherName"
              type="text"
              appLettersOnly
              class="field-control"
              [formField]="infoForm.fatherName"
            />
          </app-form-field>

          <app-form-field for="info-motherName" [label]="t().customers.detail.info.motherName">
            <input
              id="info-motherName"
              type="text"
              appLettersOnly
              class="field-control"
              [formField]="infoForm.motherName"
            />
          </app-form-field>

          <app-form-field
            for="info-identityNumber"
            [label]="t().customers.detail.info.identityNumber"
            [required]="true"
          >
            <input
              id="info-identityNumber"
              type="text"
              appDigitsOnly
              class="field-control"
              [formField]="infoForm.identityNumber"
              aria-required="true"
            />
          </app-form-field>
        </div>

        @if (saveError()) {
          <p class="mt-5 text-sm font-medium text-red-600" role="alert">
            {{ t().customers.detail.info.saveError }}
          </p>
        }

        <div class="mt-8 flex items-center justify-end gap-3 border-t border-slate-100 pt-5">
          <app-button type="button" variant="outline" size="lg" (click)="cancel()">
            {{ t().customers.detail.info.cancel }}
          </app-button>

          <app-button type="submit" size="lg" [disabled]="infoForm().invalid() || saving()">
            {{ t().customers.detail.info.save }}
          </app-button>
        </div>
      </form>
    } @else {
      <dl class="mt-5 grid gap-x-12 gap-y-5 sm:grid-cols-2">
        <div
          appDetailField
          [label]="t().customers.detail.info.firstName"
          [value]="view().firstName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.secondName"
          [value]="view().secondName ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.lastName"
          [value]="view().lastName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.birthDate"
          [value]="(view().birthDate | date: 'dd/MM/yyyy') ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.gender"
          [value]="t().customers.gender[view().gender]"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.fatherName"
          [value]="view().fatherName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.motherName"
          [value]="view().motherName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.identityNumber"
          [value]="view().identityNumber"
        ></div>
      </dl>
    }
  `
})
export class CustomerInfoPanel {
  private readonly customers = inject(CustomerService);
  private readonly router = inject(Router);

  protected readonly t = inject(I18nService).t;

  readonly customer = input.required<Customer>();

  /** Başarılı backend güncellemesinden sonra üst bileşenin detayı yeniden çekmesi için. */
  readonly saved = output<void>();

  /**
   * Salt okunur görünümün kaynağı; müşteri değişince input'a sıfırlanır. Kaydetme başarılı
   * olduğunda üst bileşen yeniden çekmediğinden burada iyimser olarak güncellenir.
   */
  protected readonly view = linkedSignal<Customer, Customer>({
    source: this.customer,
    computation: (customer) => customer
  });

  /** Silme onayı penceresinin açık olup olmadığını tutar. */
  protected readonly confirmingDelete = signal(false);

  /** Aktif ürün olduğu için silme engellendiğinde uyarı gösterilir. */
  protected readonly deleteBlocked = signal(false);

  /** Silme sürüyor mu? Çift gönderimi engeller. */
  protected readonly deleting = signal(false);

  /** Silme backend'de başarısız olursa mesaj gösterilir. */
  protected readonly deleteError = signal(false);

  /**
   * Müşterinin herhangi bir fatura hesabında aktif ürünü var mı? Varsa müşteri silinemez
   * (aktif ürün, hesaba bağlı olduğundan önce ürün/hesap kaldırılmalıdır).
   */
  protected readonly hasActiveProducts = computed(() =>
    this.customer().accounts.some((account) => (account.activeProductCount ?? 0) > 0)
  );

  /** Kaydetme sürüyor mu? Çift gönderimi engeller ve düğmeyi kilitler. */
  protected readonly saving = signal(false);

  /** Son kaydetmede backend hatası oluştu mu? */
  protected readonly saveError = signal(false);

  /** Başka bir müşteriye geçildiğinde düzenleme kapanır. */
  protected readonly isEditing = linkedSignal<Customer, boolean>({
    source: this.customer,
    computation: () => false
  });

  /** Form kaynağı; müşteri değişince güncel değerlere sıfırlanır. */
  private readonly draft = linkedSignal<Customer, CustomerDraft>({
    source: this.customer,
    computation: (customer) => toDraft(customer)
  });

  protected readonly infoForm = form(this.draft, customerDemographicSchema);

  protected startEdit(): void {
    this.draft.set(toDraft(this.view()));
    this.saveError.set(false);
    this.isEditing.set(true);
  }

  protected cancel(): void {
    this.draft.set(toDraft(this.view()));
    this.saveError.set(false);
    this.isEditing.set(false);
  }

  /**
   * Demografik alanları customer-service'e (gerçek DB) yazar. Başarıda düzenleme kapanır ve
   * yerel görünüm iyimser olarak güncellenir; hatada form açık kalır ve mesaj gösterilir.
   */
  protected save(event: Event): void {
    event.preventDefault();
    if (this.infoForm().invalid() || this.saving()) {
      return;
    }

    const draft = this.draft();
    const secondName = draft.secondName.trim() === '' ? null : draft.secondName.trim();
    const trimmedOrNull = (value: string): string | null =>
      value.trim() === '' ? null : value.trim();

    this.saving.set(true);
    this.saveError.set(false);

    this.customers
      .updateIndividual(this.customer().id, {
        firstName: draft.firstName.trim(),
        secondName,
        lastName: draft.lastName.trim(),
        birthDate: draft.birthDate,
        fatherName: trimmedOrNull(draft.fatherName),
        motherName: trimmedOrNull(draft.motherName),
        nationalityId: draft.identityNumber.trim(),
        genderType: draft.gender === 'female' ? 'FEMALE' : 'MALE'
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          // Üst bileşen yeniden çekmediğinden salt okunur görünüm iyimser güncellenir.
          this.view.update((current) => ({
            ...current,
            firstName: draft.firstName.trim(),
            secondName,
            lastName: draft.lastName.trim(),
            birthDate: draft.birthDate,
            gender: draft.gender,
            fatherName: draft.fatherName.trim(),
            motherName: draft.motherName.trim(),
            identityNumber: draft.identityNumber.trim()
          }));
          this.isEditing.set(false);
          this.saved.emit();
        },
        error: () => {
          this.saving.set(false);
          this.saveError.set(true);
        }
      });
  }

  /**
   * Silme düğmesine basıldığında: müşterinin aktif ürünü varsa silme engellenir ve uyarı
   * gösterilir; yoksa onay penceresi açılır.
   */
  protected requestDelete(): void {
    this.deleteError.set(false);
    if (this.hasActiveProducts()) {
      this.deleteBlocked.set(true);
      return;
    }
    this.deleteBlocked.set(false);
    this.confirmingDelete.set(true);
  }

  /**
   * Onay sonrası müşteriyi customer-service'te soft-delete eder (backend). Başarıda arama
   * sayfasına döner; hatada mesaj gösterir. Aktif ürün kontrolü {@link requestDelete}'te
   * yapıldığından burada tekrar doğrulanır (çift güvence).
   */
  protected deleteCustomer(): void {
    if (this.deleting()) {
      return;
    }
    if (this.hasActiveProducts()) {
      this.confirmingDelete.set(false);
      this.deleteBlocked.set(true);
      return;
    }

    this.deleting.set(true);
    this.deleteError.set(false);

    this.customers.deleteIndividual(this.customer().id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.confirmingDelete.set(false);
        void this.router.navigate(['/customers']);
      },
      error: () => {
        this.deleting.set(false);
        this.confirmingDelete.set(false);
        this.deleteError.set(true);
      }
    });
  }
}
