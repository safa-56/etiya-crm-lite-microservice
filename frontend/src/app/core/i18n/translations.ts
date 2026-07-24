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
    submitting: 'Giriş yapılıyor…',
    support: 'Sorun mu yaşıyorsunuz? Sistem yöneticinizle iletişime geçin.',
    errors: {
      // Keycloak hatalı parolayı ve kilitli hesabı bilinçli olarak ayırmaz
      // (kullanıcı adı sızdırmamak için); mesaj bu yüzden her ikisini kapsar.
      invalidCredentials:
        'Kullanıcı adı veya şifre hatalı. Art arda 5 başarısız denemede hesabınız 15 dakika kilitlenir.',
      unavailable: 'Kimlik doğrulama sunucusuna ulaşılamıyor. Lütfen daha sonra tekrar deneyin.',
      unknown: 'Giriş yapılamadı. Lütfen tekrar deneyin.'
    }
  },
  shell: {
    brand: 'Etiya',
    mainNavigation: 'Ana menü',
    version: 'EtiyaCRM Enterprise · v4.2.1',
    userMenu: 'Kullanıcı menüsü',
    logout: 'Çıkış Yap',
    // Keycloak realm rollerinin ekranda gösterilen karşılıkları; eşleşmeyen rol
    // ham kodu ile gösterilir.
    roles: {
      crm_admin: 'CRM Yöneticisi',
      crm_user: 'CRM Kullanıcısı'
    }
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
      accountNumberPlaceholder: '10 haneli, sadece rakam',
      gsm: 'GSM Numarası',
      gsmPlaceholder: 'Sadece rakam (maks 15)',
      firstName: 'Ad',
      lastName: 'Soyad',
      companyName: 'Unvan',
      startsWith: 'İle başlayan...',
      orderNumber: 'Sipariş Numarası',
      orderNumberPlaceholder: '8 haneli, sadece rakam',
      clear: 'Temizle',
      search: 'Ara'
    },
    results: {
      title: 'Arama Sonuçları',
      noMatch: 'Arama kriterlerine uygun müşteri bulunamadı.',
      count: 'kayıt bulundu',
      countBadge: 'kayıt',
      customerId: 'Müşteri ID',
      customer: 'Müşteri',
      type: 'Tip',
      identity: 'TCKN / VKN',
      gsm: 'GSM',
      city: 'Şehir',
      status: 'Durum',
      openDetail: 'Müşteri detayını aç',
      firstName: 'Ad',
      secondName: 'İkinci Ad',
      lastName: 'Soyad',
      role: 'Rol',
      loading: 'Sonuçlar yükleniyor…',
      loadError: 'Arama sırasında bir hata oluştu. Lütfen tekrar deneyin.',
      // `{count}` yerine toplam kayıt sayısı geçer.
      pagedInfo: '{count} sonuç bulundu. Sayfalar halinde listeleniyor.',
      // `{from}`/`{to}` görünen aralık, `{total}` toplam kayıt sayısıdır.
      range: '{total} kayıttan {from}–{to} arası',
      pageNav: 'Sayfa gezinmesi',
      pageLabel: 'Sayfa',
      prevPage: 'Önceki sayfa',
      nextPage: 'Sonraki sayfa',
      roleLabels: {
        b2c: 'Müşteri',
        b2b: 'Kurumsal'
      }
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
      back: 'Geri',
      create: 'Oluştur',
      submitError: 'Müşteri oluşturulamadı. Bilgileri kontrol edip tekrar deneyin.',
      validationTitle: 'Lütfen aşağıdaki alanları düzeltin:',
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
      loading: 'Müşteri yükleniyor…',
      loadError: 'Müşteri bilgileri yüklenemedi.',
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
        save: 'Kaydet',
        cancel: 'İptal',
        deleteConfirm: 'Bu müşteriyi silmek istediğinize emin misiniz?',
        confirmYes: 'Evet',
        confirmNo: 'Hayır',
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
        productPreview: 'Ürün Önizleme',
        previewOfferId: 'Ürün Teklif ID',
        previewOfferName: 'Ürün Teklif Adı',
        previewSpecId: 'Ürün Spec ID',
        previewCharacteristics: 'Ürün Özellikleri',
        previewAddressName: 'Adres Adı',
        previewBuildingNo: 'Bina / Daire No',
        previewNoAddress: 'Bu ürün için hizmet adresi tanımlı değil.',
        previewEmpty: 'Bu ürün için önizleme bilgisi bulunmuyor.',
        previewClose: 'Önizlemeyi kapat',
        noProducts: 'Bu hesapta ürün bulunmuyor.',
        newSale: 'Yeni Satış Başlat',
        transfer: 'Devret',
        changeServiceAddress: 'Hizmet Adresi Değişikliği',
        pagination: 'Hesap sayfaları',
        page: 'Sayfa',
        createTitle: 'Fatura Hesabı Oluştur',
        editTitle: 'Fatura Hesabını Düzenle',
        accountName: 'Hesap Adı',
        accountDescription: 'Hesap Açıklaması',
        addressInfo: 'Adres Bilgisi',
        addAddress: 'Yeni Adres Ekle',
        formCancel: 'İptal',
        formCreate: 'Oluştur',
        formSave: 'Kaydet',
        deleteConfirm: 'Bu fatura hesabını silmek istediğinize emin misiniz?',
        confirmYes: 'Evet',
        confirmNo: 'Hayır'
      },
      addresses: {
        title: 'Adres',
        add: 'Yeni Adres Ekle',
        menu: 'Adres işlemleri',
        primary: 'Birincil adres yap',
        edit: 'Düzenle',
        delete: 'Sil',
        editTitle: 'Adres Düzenle',
        city: 'Şehir',
        cityPlaceholder: 'Seçiniz',
        street: 'Sokak / Cadde',
        buildingNo: 'Bina / Daire No',
        description: 'Adres Açıklaması',
        cancel: 'İptal',
        save: 'Kaydet'
      },
      contact: {
        title: 'İletişim Kanalı',
        edit: 'İletişim kanalını düzenle',
        email: 'E-posta',
        mobilePhone: 'Cep Telefonu',
        homePhone: 'Ev Telefonu',
        fax: 'Faks',
        countryCode: 'Ülke kodu',
        cancel: 'İptal',
        save: 'Kaydet',
        emailPlaceholder: 'ornek@etiya.com',
        mobilePlaceholder: '10 haneli numara'
      }
    }
  },
  sales: {
    subtitle: 'Müşteri Yönetim Sistemi',
    breadcrumbAccount: 'Müşteri Hesabı',
    currency: 'TL',
    offer: {
      title: 'Teklif Seçimi',
      catalog: 'Katalog',
      campaign: 'Kampanya',
      catalogSelect: 'Katalog Seçimi',
      campaignSelect: 'Kampanya Seçimi',
      select: 'Seçiniz',
      offerId: 'Ürün Teklif ID',
      offerName: 'Ürün Teklif Adı',
      campaignId: 'Kampanya ID',
      campaignName: 'Kampanya Adı',
      search: 'Ara',
      addToCart: 'Sepete Ekle'
    },
    cart: {
      title: 'Sepet',
      empty: 'Sepet boş',
      total: 'Toplam Tutar',
      clear: 'Temizle',
      removeItem: 'Ürünü sepetten çıkar',
      removeCampaign: 'Kampanyayı sepetten çıkar',
      campaign: 'Kampanya',
      groupTotal: 'Ara toplam'
    },
    config: {
      title: 'Ürün Konfigürasyonu',
      empty: 'Seçilen ürünler için ek konfigürasyon gerekmiyor.',
      items: 'Seçilen Ürünler',
      addressInfo: 'Adres Bilgisi',
      addressHint: 'Mevcut Adreslerden Bir Hizmet Adresi Seçin',
      addAddress: 'Yeni Adres Ekle'
    },
    submit: {
      title: 'Sipariş Gönder',
      orderId: 'Sipariş ID',
      items: 'Sipariş Kalemleri',
      serviceAddress: 'Hizmet Adresi',
      total: 'Toplam Tutar',
      send: 'Gönder'
    },
    success: {
      title: 'Sipariş oluşturuldu!',
      message: 'Siparişiniz başarıyla oluşturuldu ve işleme alındı.',
      orderId: 'Sipariş ID',
      backToSearch: 'Müşteri Aramaya Dön'
    },
    next: 'İleri',
    back: 'Geri'
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
    submitting: 'Signing in…',
    support: 'Having trouble? Contact your system administrator.',
    errors: {
      invalidCredentials:
        'Invalid username or password. After 5 consecutive failed attempts your account is locked for 15 minutes.',
      unavailable: 'The authentication server is unreachable. Please try again later.',
      unknown: 'Sign-in failed. Please try again.'
    }
  },
  shell: {
    brand: 'Etiya',
    mainNavigation: 'Main navigation',
    version: 'EtiyaCRM Enterprise · v4.2.1',
    userMenu: 'User menu',
    logout: 'Sign Out',
    roles: {
      crm_admin: 'CRM Administrator',
      crm_user: 'CRM User'
    }
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
      accountNumberPlaceholder: '10 digits, numbers only',
      gsm: 'Mobile Number',
      gsmPlaceholder: 'Digits only (max 15)',
      firstName: 'First Name',
      lastName: 'Last Name',
      companyName: 'Company Name',
      startsWith: 'Starts with...',
      orderNumber: 'Order Number',
      orderNumberPlaceholder: '8 digits, numbers only',
      clear: 'Clear',
      search: 'Search'
    },
    results: {
      title: 'Search Results',
      noMatch: 'No customer matches the search criteria.',
      count: 'records found',
      countBadge: 'records',
      customerId: 'Customer ID',
      customer: 'Customer',
      type: 'Type',
      identity: 'National / Tax ID',
      gsm: 'Mobile',
      city: 'City',
      status: 'Status',
      openDetail: 'Open customer detail',
      firstName: 'First Name',
      secondName: 'Second Name',
      lastName: 'Last Name',
      role: 'Role',
      loading: 'Loading results…',
      loadError: 'Something went wrong while searching. Please try again.',
      // `{count}` is replaced by the total number of records.
      pagedInfo: '{count} results found. Listed across pages.',
      // `{from}`/`{to}` are the visible range, `{total}` the total record count.
      range: '{from}–{to} of {total} records',
      pageNav: 'Page navigation',
      pageLabel: 'Page',
      prevPage: 'Previous page',
      nextPage: 'Next page',
      roleLabels: {
        b2c: 'Customer',
        b2b: 'Corporate'
      }
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
      back: 'Back',
      create: 'Create',
      submitError: 'Could not create the customer. Check the details and try again.',
      validationTitle: 'Please fix the following fields:',
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
      loading: 'Loading customer…',
      loadError: 'Could not load customer information.',
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
        save: 'Save',
        cancel: 'Cancel',
        deleteConfirm: 'Are you sure to delete this customer?',
        confirmYes: 'Yes',
        confirmNo: 'No',
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
        productPreview: 'Product Preview',
        previewOfferId: 'Product Offer ID',
        previewOfferName: 'Product Offer Name',
        previewSpecId: 'Product Spec ID',
        previewCharacteristics: 'Product Characteristics',
        previewAddressName: 'Address Name',
        previewBuildingNo: 'House / Flat No',
        previewNoAddress: 'No service address is defined for this product.',
        previewEmpty: 'No preview information is available for this product.',
        previewClose: 'Close preview',
        noProducts: 'This account has no products.',
        newSale: 'Start New Sale',
        transfer: 'Transfer',
        changeServiceAddress: 'Change Service Address',
        pagination: 'Account pages',
        page: 'Page',
        createTitle: 'Create Billing Account',
        editTitle: 'Edit Billing Account',
        accountName: 'Account Name',
        accountDescription: 'Account Description',
        addressInfo: 'Address Information',
        addAddress: 'Add New Address',
        formCancel: 'Cancel',
        formCreate: 'Create',
        formSave: 'Save',
        deleteConfirm: 'Are you sure to delete this billing account?',
        confirmYes: 'Yes',
        confirmNo: 'No'
      },
      addresses: {
        title: 'Address',
        add: 'Add New Address',
        menu: 'Address actions',
        primary: 'Set as primary address',
        edit: 'Edit',
        delete: 'Delete',
        editTitle: 'Edit Address',
        city: 'City',
        cityPlaceholder: 'Select',
        street: 'Street / Avenue',
        buildingNo: 'Building / Apartment No',
        description: 'Address Description',
        cancel: 'Cancel',
        save: 'Save'
      },
      contact: {
        title: 'Contact Channel',
        edit: 'Edit contact channel',
        email: 'E-mail',
        mobilePhone: 'Mobile Phone',
        homePhone: 'Home Phone',
        fax: 'Fax',
        countryCode: 'Country code',
        cancel: 'Cancel',
        save: 'Save',
        emailPlaceholder: 'example@etiya.com',
        mobilePlaceholder: '10-digit number'
      }
    }
  },
  sales: {
    subtitle: 'Customer Management System',
    breadcrumbAccount: 'Customer Account',
    currency: 'TL',
    offer: {
      title: 'Offer Selection',
      catalog: 'Catalog',
      campaign: 'Campaign',
      catalogSelect: 'Catalog Selection',
      campaignSelect: 'Campaign Selection',
      select: 'Select',
      offerId: 'Product Offer ID',
      offerName: 'Product Offer Name',
      campaignId: 'Campaign ID',
      campaignName: 'Campaign Name',
      search: 'Search',
      addToCart: 'Add to Cart'
    },
    cart: {
      title: 'Cart',
      empty: 'Cart is empty',
      total: 'Total Amount',
      clear: 'Clear',
      removeItem: 'Remove product from cart',
      removeCampaign: 'Remove campaign from cart',
      campaign: 'Campaign',
      groupTotal: 'Subtotal'
    },
    config: {
      title: 'Product Configuration',
      empty: 'The selected products require no additional configuration.',
      items: 'Selected Products',
      addressInfo: 'Address Information',
      addressHint: 'Select a Service Address from Existing Addresses',
      addAddress: 'Add New Address'
    },
    submit: {
      title: 'Send Order',
      orderId: 'Order ID',
      items: 'Order Items',
      serviceAddress: 'Service Address',
      total: 'Total Amount',
      send: 'Send'
    },
    success: {
      title: 'Order created!',
      message: 'Your order has been created successfully and is being processed.',
      orderId: 'Order ID',
      backToSearch: 'Back to Customer Search'
    },
    next: 'Next',
    back: 'Back'
  }
};

export type Translations = typeof tr;

/** Üst başlıkta gösterilecek sayfa anahtarları (route `data.pageKey` ile eşleşir). */
export type PageKey = keyof Translations['pages'];

export const TRANSLATIONS: Record<Language, Translations> = { tr, en };
