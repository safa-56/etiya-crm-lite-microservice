import { Routes } from '@angular/router';

import { authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
    // Oturumu açık olan kullanıcı giriş ekranında beklemez, uygulamaya alınır.
    canActivate: [guestGuard],
    title: 'Etiya CRM'
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout').then((m) => m.MainLayout),
    // Kabuğun altındaki tüm sayfalar geçerli bir oturum ister.
    canActivate: [authGuard],
    children: [
      {
        path: 'customers',
        loadComponent: () => import('./features/customers/customers').then((m) => m.Customers),
        data: { pageKey: 'customers' },
        title: 'Müşteri Arama · Etiya CRM'
      },
      {
        // 'customers/:id' kuralından önce gelmeli; aksi hâlde 'new' bir id sanılır.
        path: 'customers/new',
        loadComponent: () =>
          import('./features/customers/customer-create/customer-create').then(
            (m) => m.CustomerCreate
          ),
        data: { pageKey: 'customerCreate' },
        title: 'Müşteri Oluştur · Etiya CRM'
      },
      {
        // 'customers/:id' kuralından önce gelmeli; aksi hâlde ':id' üç segmenti yakalamaya çalışır.
        path: 'customers/:id/new-sale',
        loadComponent: () =>
          import('./features/customers/new-sale/new-sale').then((m) => m.NewSale),
        title: 'Yeni Satış · Etiya CRM'
      },
      {
        path: 'customers/:id',
        loadComponent: () =>
          import('./features/customers/customer-detail/customer-detail').then(
            (m) => m.CustomerDetail
          ),
        data: { pageKey: 'customerDetail' },
        title: 'Müşteri Bilgisi · Etiya CRM'
      }
    ]
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

 Kural 3: path: '' + children — "layout route". Burada path boş olduğu için adres çubuğuna bir
 şey eklemez; tek işi çocuk sayfaların etrafını MainLayout (sol menü + üst bar) ile sarmaktır.
 Böylece /customers adresi hem menüyü hem sayfayı gösterir, login sayfası ise menüsüz kalır.
 data: { pageKey: 'customers' } → üst bardaki başlık bu anahtardan çözülür (bkz. MainLayout).

 Kural 4: path: '**' — "yukarıdakilerin hiçbiri tutmadıysa". Bilinmeyen bir adrese giden
 kullanıcı hata görmek yerine login'e yönlendirilir. Sıralama kritiktir: ** kuralı en sonda
 olmak zorundadır, çünkü Angular kuralları yukarıdan aşağıya dener ve ilk eşleşende durur.
 Yukarı koyarsanız hiçbir sayfa açılmaz.
 */
