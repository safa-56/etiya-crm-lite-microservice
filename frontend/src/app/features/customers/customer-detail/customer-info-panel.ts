import { Component, inject, input } from '@angular/core';
import { DatePipe } from '@angular/common';

import { I18nService } from '../../../core/i18n/i18n.service';
import { DetailField } from '../../../shared/ui/detail-field/detail-field';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { Customer } from '../customer.model';

/** "Müşteri Bilgisi" sekmesi: demografik alanların salt okunur listesi. */
@Component({
  selector: 'app-customer-info-panel',
  imports: [DatePipe, DetailField, IconButton, PanelHeader],
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
        <app-icon-button icon="pencil" tone="edit" [label]="t().customers.detail.info.edit" />
        <app-icon-button icon="trash" tone="danger" [label]="t().customers.detail.info.delete" />
      </ng-container>
    </app-panel-header>

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
  `
})
export class CustomerInfoPanel {
  protected readonly t = inject(I18nService).t;

  readonly customer = input.required<Customer>();
}
