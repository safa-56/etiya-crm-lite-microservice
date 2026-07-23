import { Component, inject, input, linkedSignal, signal } from '@angular/core';
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
    secondName: customer.secondName ?? '',
    lastName: customer.lastName,
    birthDate: customer.birthDate,
    gender: customer.gender,
    fatherName: customer.fatherName,
    motherName: customer.motherName,
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
            (click)="confirmingDelete.set(true)"
          />
        }
      </ng-container>
    </app-panel-header>

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

        <div class="mt-8 flex items-center justify-end gap-3 border-t border-slate-100 pt-5">
          <app-button type="button" variant="outline" size="lg" (click)="cancel()">
            {{ t().customers.detail.info.cancel }}
          </app-button>

          <app-button type="submit" size="lg" [disabled]="infoForm().invalid()">
            {{ t().customers.detail.info.save }}
          </app-button>
        </div>
      </form>
    } @else {
      <dl class="mt-5 grid gap-x-12 gap-y-5 sm:grid-cols-2">
        <div
          appDetailField
          [label]="t().customers.detail.info.firstName"
          [value]="customer().firstName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.secondName"
          [value]="customer().secondName ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.lastName"
          [value]="customer().lastName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.birthDate"
          [value]="(customer().birthDate | date: 'dd/MM/yyyy') ?? t().common.empty"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.gender"
          [value]="t().customers.gender[customer().gender]"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.fatherName"
          [value]="customer().fatherName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.motherName"
          [value]="customer().motherName"
        ></div>
        <div
          appDetailField
          [label]="t().customers.detail.info.identityNumber"
          [value]="customer().identityNumber"
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

  /** Silme onayı penceresinin açık olup olmadığını tutar. */
  protected readonly confirmingDelete = signal(false);

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
    this.draft.set(toDraft(this.customer()));
    this.isEditing.set(true);
  }

  protected cancel(): void {
    this.draft.set(toDraft(this.customer()));
    this.isEditing.set(false);
  }

  protected save(event: Event): void {
    event.preventDefault();
    if (this.infoForm().invalid()) {
      return;
    }

    const draft = this.draft();
    this.customers.update(this.customer().id, {
      ...draft,
      secondName: draft.secondName.trim() === '' ? null : draft.secondName
    });
    this.isEditing.set(false);
  }

  /** Onay sonrası müşteriyi siler ve arama sayfasına döner. */
  protected deleteCustomer(): void {
    this.customers.remove(this.customer().id);
    this.confirmingDelete.set(false);
    void this.router.navigate(['/customers']);
  }
}
