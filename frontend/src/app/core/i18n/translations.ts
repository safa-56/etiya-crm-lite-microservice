export const LANGUAGES = ['tr', 'en'] as const;

export type Language = (typeof LANGUAGES)[number];

export const DEFAULT_LANGUAGE: Language = 'tr';

const tr = {
  common: {
    languageSelector: 'Dil seçimi',
    empty: '—'
  },
  login: {
    brandTagline: 'Kurumsal Portal',
    title: 'Müşteri Yönetim Sistemi',
    username: 'Kullanıcı Adı',
    usernamePlaceholder: 'Kullanıcı adı girin',
    password: 'Şifre',
    passwordPlaceholder: 'Şifre girin',
    showPassword: 'Şifreyi göster',
    hidePassword: 'Şifreyi gizle',
    submit: 'Giriş Yap',
    support: 'Sorun mu yaşıyorsunuz? Sistem yöneticinizle iletişime geçin.'
  },
  shell: {
    brand: 'Etiya',
    mainNavigation: 'Ana menü',
    version: 'EtiyaCRM Enterprise · v4.2.1',
    userMenu: 'Kullanıcı menüsü'
  },
  nav: {
    customerSearch: 'Müşteri Arama',
    customerInfo: 'Müşteri Bilgisi',
    customerCreate: 'Müşteri Oluştur',
    orders: 'Siparişler',
    reports: 'Raporlar',
    settings: 'Ayarlar',
    logout: 'Çıkış Yap'
  },
  pages: {
    customers: {
      title: 'Müşteri Arama',
      subtitle: 'Müşteri Yönetim Sistemi'
    },
    customerDetail: {
      title: 'Müşteri Bilgisi',
      subtitle: 'Müşteri Yönetim Sistemi'
    },
    customerCreate: {
      title: 'Müşteri Oluştur',
      subtitle: 'Müşteri Yönetim Sistemi'
    }
  },
  customers: {
    filters: {
      title: 'Arama Filtreleri',
      customerType: 'Müşteri tipi',
      b2c: 'B2C',
      b2b: 'B2B',
      identityNumber: 'TC Kimlik No',
      identityNumberPlaceholder: '11 haneli TC kimlik no',
      taxNumber: 'Vergi Kimlik No',
      taxNumberPlaceholder: '10 haneli vergi kimlik no',
      customerId: 'Müşteri ID',
      customerIdPlaceholder: 'Sayısal müşteri ID',
      accountNumber: 'Hesap Numarası',
      accountNumberPlaceholder: 'Sadece rakam (maks 30)',
      gsm: 'GSM Numarası',
      gsmPlaceholder: '10-15 haneli mobil no.',
      firstName: 'Ad',
      lastName: 'Soyad',
      companyName: 'Unvan',
      startsWith: 'İle başlayan...',
      orderNumber: 'Sipariş Numarası',
      orderNumberPlaceholder: 'Sadece rakam (maks 20)',
      clear: 'Temizle',
      search: 'Ara'
    },
    results: {
      title: 'Arama Sonuçları',
      noMatch: 'Arama kriterlerine uygun müşteri bulunamadı.',
      count: 'kayıt bulundu',
      customerId: 'Müşteri ID',
      customer: 'Müşteri',
      type: 'Tip',
      identity: 'TCKN / VKN',
      gsm: 'GSM',
      city: 'Şehir',
      status: 'Durum',
      openDetail: 'Müşteri detayını aç'
    },
    status: {
      active: 'Aktif',
      passive: 'Pasif'
    },
    gender: {
      male: 'Erkek',
      female: 'Kadın'
    },
    create: {
      breadcrumb: 'Sayfa yolu',
      startsWith: 'İle başlayan...',
      cancel: 'İptal',
      next: 'İleri',
      steps: {
        demographic: 'Demografik Bilgi',
        address: 'Adres',
        contact: 'İletişim Kanalı'
      },
      demographic: {
        title: 'Demografik Bilgi',
        firstName: 'Ad',
        secondName: 'İkinci Ad',
        lastName: 'Soyad',
        birthDate: 'Doğum Tarihi',
        gender: 'Cinsiyet',
        fatherName: 'Baba Adı',
        motherName: 'Anne Adı',
        identityNumber: 'Uyruk / TC No'
      }
    },
    detail: {
      breadcrumb: 'Sayfa yolu',
      role: 'Müşteri',
      /** `{date}` yerine "Temmuz 2021" gibi ay-yıl metni geçer. */
      memberSince: "{date}'den beri",
      notFound: 'Müşteri bulunamadı.',
      backToSearch: 'Müşteri aramaya dön',
      tabs: {
        label: 'Müşteri detay sekmeleri',
        info: 'Müşteri Bilgisi',
        account: 'Müşteri Hesabı',
        address: 'Adres',
        contact: 'İletişim Kanalı'
      },
      info: {
        title: 'Müşteri Bilgisi',
        edit: 'Müşteri bilgisini düzenle',
        delete: 'Müşteriyi sil',
        firstName: 'Ad',
        secondName: 'İkinci Ad',
        lastName: 'Soyad',
        birthDate: 'Doğum Tarihi',
        gender: 'Cinsiyet',
        fatherName: 'Baba Adı',
        motherName: 'Anne Adı',
        identityNumber: 'Uyruk / TC No'
      },
      accounts: {
        title: 'Müşteri Hesapları',
        create: 'Yeni Hesap Oluştur',
        status: 'Hesap Durumu',
        number: 'Hesap Numarası',
        name: 'Hesap Adı',
        type: 'Hesap Türü',
        actions: 'İşlem',
        expand: 'Hesap ürünlerini göster',
        collapse: 'Hesap ürünlerini gizle',
        edit: 'Hesabı düzenle',
        delete: 'Hesabı sil',
        empty: 'Bu müşteriye ait hesap bulunmuyor.',
        productId: 'Ürün ID',
        productName: 'Ürün / Aksiyon',
        campaignName: 'Kampanya Adı',
        campaignId: 'Kampanya ID',
        productDelete: 'Ürünü sil',
        productDetail: 'Ürün detayını görüntüle',
        noProducts: 'Bu hesapta ürün bulunmuyor.',
        newSale: 'Yeni Satış Başlat',
        transfer: 'Devret',
        changeServiceAddress: 'Hizmet Adresi Değişikliği',
        pagination: 'Hesap sayfaları',
        page: 'Sayfa'
      },
      addresses: {
        title: 'Adres',
        add: 'Yeni Adres Ekle',
        menu: 'Adres işlemleri',
        primary: 'Birincil adres yap'
      },
      contact: {
        title: 'İletişim Kanalı',
        edit: 'İletişim kanalını düzenle',
        email: 'E-posta',
        mobilePhone: 'Cep Telefonu',
        homePhone: 'Ev Telefonu',
        fax: 'Faks'
      }
    }
  }
};

/** İngilizce sözlük, Türkçe sözlüğün yapısını birebir izlemek zorundadır. */
const en: typeof tr = {
  common: {
    languageSelector: 'Language selection',
    empty: '—'
  },
  login: {
    brandTagline: 'Enterprise Portal',
    title: 'Customer Management System',
    username: 'Username',
    usernamePlaceholder: 'Enter your username',
    password: 'Password',
    passwordPlaceholder: 'Enter your password',
    showPassword: 'Show password',
    hidePassword: 'Hide password',
    submit: 'Sign In',
    support: 'Having trouble? Contact your system administrator.'
  },
  shell: {
    brand: 'Etiya',
    mainNavigation: 'Main navigation',
    version: 'EtiyaCRM Enterprise · v4.2.1',
    userMenu: 'User menu'
  },
  nav: {
    customerSearch: 'Customer Search',
    customerInfo: 'Customer Information',
    customerCreate: 'Create Customer',
    orders: 'Orders',
    reports: 'Reports',
    settings: 'Settings',
    logout: 'Sign Out'
  },
  pages: {
    customers: {
      title: 'Customer Search',
      subtitle: 'Customer Management System'
    },
    customerDetail: {
      title: 'Customer Information',
      subtitle: 'Customer Management System'
    },
    customerCreate: {
      title: 'Create Customer',
      subtitle: 'Customer Management System'
    }
  },
  customers: {
    filters: {
      title: 'Search Filters',
      customerType: 'Customer type',
      b2c: 'B2C',
      b2b: 'B2B',
      identityNumber: 'National ID',
      identityNumberPlaceholder: '11-digit national ID',
      taxNumber: 'Tax ID',
      taxNumberPlaceholder: '10-digit tax ID',
      customerId: 'Customer ID',
      customerIdPlaceholder: 'Numeric customer ID',
      accountNumber: 'Account Number',
      accountNumberPlaceholder: 'Digits only (max 30)',
      gsm: 'Mobile Number',
      gsmPlaceholder: '10-15 digit mobile no.',
      firstName: 'First Name',
      lastName: 'Last Name',
      companyName: 'Company Name',
      startsWith: 'Starts with...',
      orderNumber: 'Order Number',
      orderNumberPlaceholder: 'Digits only (max 20)',
      clear: 'Clear',
      search: 'Search'
    },
    results: {
      title: 'Search Results',
      noMatch: 'No customer matches the search criteria.',
      count: 'records found',
      customerId: 'Customer ID',
      customer: 'Customer',
      type: 'Type',
      identity: 'National / Tax ID',
      gsm: 'Mobile',
      city: 'City',
      status: 'Status',
      openDetail: 'Open customer detail'
    },
    status: {
      active: 'Active',
      passive: 'Passive'
    },
    gender: {
      male: 'Male',
      female: 'Female'
    },
    create: {
      breadcrumb: 'Breadcrumb',
      startsWith: 'Starts with...',
      cancel: 'Cancel',
      next: 'Next',
      steps: {
        demographic: 'Demographic Information',
        address: 'Address',
        contact: 'Contact Channel'
      },
      demographic: {
        title: 'Demographic Information',
        firstName: 'First Name',
        secondName: 'Middle Name',
        lastName: 'Last Name',
        birthDate: 'Date of Birth',
        gender: 'Gender',
        fatherName: "Father's Name",
        motherName: "Mother's Name",
        identityNumber: 'Nationality / National ID'
      }
    },
    detail: {
      breadcrumb: 'Breadcrumb',
      role: 'Customer',
      memberSince: 'Customer since {date}',
      notFound: 'Customer not found.',
      backToSearch: 'Back to customer search',
      tabs: {
        label: 'Customer detail tabs',
        info: 'Customer Information',
        account: 'Customer Account',
        address: 'Address',
        contact: 'Contact Channel'
      },
      info: {
        title: 'Customer Information',
        edit: 'Edit customer information',
        delete: 'Delete customer',
        firstName: 'First Name',
        secondName: 'Middle Name',
        lastName: 'Last Name',
        birthDate: 'Date of Birth',
        gender: 'Gender',
        fatherName: "Father's Name",
        motherName: "Mother's Name",
        identityNumber: 'Nationality / National ID'
      },
      accounts: {
        title: 'Customer Accounts',
        create: 'Create New Account',
        status: 'Account Status',
        number: 'Account Number',
        name: 'Account Name',
        type: 'Account Type',
        actions: 'Actions',
        expand: 'Show account products',
        collapse: 'Hide account products',
        edit: 'Edit account',
        delete: 'Delete account',
        empty: 'This customer has no accounts.',
        productId: 'Product ID',
        productName: 'Product / Action',
        campaignName: 'Campaign Name',
        campaignId: 'Campaign ID',
        productDelete: 'Delete product',
        productDetail: 'View product detail',
        noProducts: 'This account has no products.',
        newSale: 'Start New Sale',
        transfer: 'Transfer',
        changeServiceAddress: 'Change Service Address',
        pagination: 'Account pages',
        page: 'Page'
      },
      addresses: {
        title: 'Address',
        add: 'Add New Address',
        menu: 'Address actions',
        primary: 'Set as primary address'
      },
      contact: {
        title: 'Contact Channel',
        edit: 'Edit contact channel',
        email: 'E-mail',
        mobilePhone: 'Mobile Phone',
        homePhone: 'Home Phone',
        fax: 'Fax'
      }
    }
  }
};

export type Translations = typeof tr;

/** Üst başlıkta gösterilecek sayfa anahtarları (route `data.pageKey` ile eşleşir). */
export type PageKey = keyof Translations['pages'];

export const TRANSLATIONS: Record<Language, Translations> = { tr, en };
