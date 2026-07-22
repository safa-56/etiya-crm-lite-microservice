import { Service, signal } from '@angular/core';

/** Üst bar başlığı için içerik. */
export interface ShellTitleContent {
  readonly title: string;
  readonly subtitle: string;
}

/**
 * Route `pageKey`'ine bağlı olmayan sayfaların (örn. çok adımlı satış akışı) üst bar başlığını
 * geçici olarak değiştirebilmesi için kullanılır. `null` iken varsayılan pageKey başlığı geçerlidir.
 */
@Service()
export class ShellTitle {
  readonly override = signal<ShellTitleContent | null>(null);
}
