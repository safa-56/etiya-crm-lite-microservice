import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
    title: 'Etiya CRM'
  },
  { path: '**', redirectTo: 'login' }
];

/*
 Kural 1: path: '' — kök adres (/). pathMatch: 'full' → URL'nin tamamı boş olmalı; sadece başı
 boş olan (/login gibi) eşleşmesin. Bu olmadan kural her URL'yi yakalar ve sonsuz yönlendirmeye
 girersiniz. redirectTo: 'login' → /login'e gönder.

 Kural 2: path: 'login' — asıl sayfa.
 loadComponent: () => import(...) → lazy loading. Bu satırın anlamı: "Login sayfasının kodunu
 şimdi indirme; kullanıcı gerçekten /login'e gittiğinde indir." Angular bu bileşeni derleme
 sırasında ayrı bir dosyaya (chunk) koyar. Şu an tek sayfa olduğu için kazancı sınırlı,
 ama proje 20 sayfaya çıktığında kullanıcı sadece açtığı sayfayı indirir — ilk açılış hızlı
 kalır.
 .then((m) => m.Login) → indirilen dosyadan Login sınıfını çıkarır.
 title: 'Etiya CRM' → bu sayfaya girildiğinde tarayıcı sekmesinin başlığı bu olur. Angular bunu
 otomatik ayarlar.

 Kural 3: path: '**' — "yukarıdakilerin hiçbiri tutmadıysa". Bilinmeyen bir adrese giden
 kullanıcı hata görmek yerine login'e yönlendirilir. Sıralama kritiktir: ** kuralı en sonda
 olmak zorundadır, çünkü Angular kuralları yukarıdan aşağıya dener ve ilk eşleşende durur.
 Yukarı koyarsanız hiçbir sayfa açılmaz.
 */
