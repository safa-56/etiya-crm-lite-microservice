import { Component, inject, input } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { DetailField } from '../../../shared/ui/detail-field/detail-field';
import { IconButton } from '../../../shared/ui/icon-button/icon-button';
import { PanelHeader } from '../../../shared/ui/panel-header/panel-header';
import { CustomerContact } from '../customer.model';

/** "İletişim Kanalı" sekmesi. */
@Component({
  selector: 'app-customer-contact-panel',
  imports: [DetailField, IconButton, PanelHeader],
  host: {
    role: 'tabpanel',
    id: 'panel-contact',
    'aria-labelledby': 'tab-contact',
    tabindex: '0',
    class: 'block rounded-2xl border border-slate-200 bg-white px-6 py-5 shadow-sm'
  },
  template: `
    <app-panel-header [heading]="t().customers.detail.contact.title">
      <app-icon-button
        inline
        icon="pencil"
        tone="edit"
        [label]="t().customers.detail.contact.edit"
      />
    </app-panel-header>

    <dl class="mt-5 grid gap-x-12 gap-y-5 sm:grid-cols-2">
      <div
        appDetailField
        [breakAll]="true"
        [label]="t().customers.detail.contact.email"
        [value]="contact().email ?? t().common.empty"
      ></div>
      <div
        appDetailField
        [label]="t().customers.detail.contact.homePhone"
        [value]="contact().homePhone ?? t().common.empty"
      ></div>
      <div
        appDetailField
        [label]="t().customers.detail.contact.mobilePhone"
        [value]="contact().mobilePhone ?? t().common.empty"
      ></div>
      <div
        appDetailField
        [label]="t().customers.detail.contact.fax"
        [value]="contact().fax ?? t().common.empty"
      ></div>
    </dl>
  `
})
export class CustomerContactPanel {
  protected readonly t = inject(I18nService).t;

  readonly contact = input.required<CustomerContact>();
}
