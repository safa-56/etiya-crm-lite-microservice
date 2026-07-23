import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideZonelessChangeDetection } from '@angular/core';

import { Sidebar } from './sidebar';

/**
 * Çıkış satırının bir **eylem** olduğunu doğrular: gezinme bağlantısı değil, düğme
 * üretmeli ve tıklandığında kabuğa `logout` sinyali göndermelidir. Bağlantı olsaydı
 * oturum temizlenmeden /login'e gidilir, guestGuard kullanıcıyı geri gönderirdi.
 */
describe('Sidebar', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideRouter([])]
    });
  });

  function render() {
    const fixture = TestBed.createComponent(Sidebar);
    fixture.componentRef.setInput('pageKey', 'customers');
    fixture.detectChanges();
    return fixture;
  }

  /** Menüdeki çıkış satırının düğmesini metnine göre bulur. */
  function logoutButton(root: HTMLElement): HTMLButtonElement {
    const buttons = Array.from(root.querySelectorAll('button'));
    const target = buttons.find((button) => button.textContent?.includes('Çıkış Yap'));

    expect(target).toBeDefined();
    return target as HTMLButtonElement;
  }

  it('çıkış satırı bağlantı değil düğme olarak render edilir', () => {
    const fixture = render();
    const root = fixture.nativeElement as HTMLElement;

    const links = Array.from(root.querySelectorAll('a'));
    expect(links.some((link) => link.textContent?.includes('Çıkış Yap'))).toBe(false);
    expect(logoutButton(root)).toBeTruthy();
  });

  it('çıkışa tıklandığında logout sinyali yayınlanır', () => {
    const fixture = render();
    let emitted = 0;
    fixture.componentInstance.logout.subscribe(() => (emitted += 1));

    logoutButton(fixture.nativeElement as HTMLElement).click();

    expect(emitted).toBe(1);
  });
});
