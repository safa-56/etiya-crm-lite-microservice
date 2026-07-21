import { Component, computed, inject, signal } from '@angular/core';

import { I18nService } from '../../../core/i18n/i18n.service';
import { Breadcrumb, BreadcrumbItem } from '../../../shared/ui/breadcrumb/breadcrumb';
import { Stepper, StepperItem } from '../../../shared/ui/stepper/stepper';
import { CustomerDemographicForm } from './customer-demographic-form';

/** Müşteri oluşturma sihirbazı; adım durumunu tutar, adım formlarını çizer. */
@Component({
  selector: 'app-customer-create',
  imports: [Breadcrumb, Stepper, CustomerDemographicForm],
  templateUrl: './customer-create.html'
})
export class CustomerCreate {
  protected readonly t = inject(I18nService).t;

  /** Adres ve iletişim adımları tasarlandığında bu değer ilerletilecek. */
  protected readonly activeStep = signal(1);

  protected readonly steps = computed<readonly StepperItem[]>(() => {
    const steps = this.t().customers.create.steps;

    return [
      { index: 1, label: steps.demographic },
      { index: 2, label: steps.address },
      { index: 3, label: steps.contact }
    ];
  });

  protected readonly breadcrumb = computed<readonly BreadcrumbItem[]>(() => [
    { label: this.t().nav.customerSearch, link: '/customers' },
    { label: this.t().nav.customerCreate }
  ]);

  protected goToStep(step: number): void {
    this.activeStep.set(step);
  }
}
