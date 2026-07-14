export const LANGUAGES = ['tr', 'en'] as const;

export type Language = (typeof LANGUAGES)[number];

export const DEFAULT_LANGUAGE: Language = 'tr';

const tr = {
  common: {
    languageSelector: 'Dil seçimi'
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
  }
};

/** İngilizce sözlük, Türkçe sözlüğün yapısını birebir izlemek zorundadır. */
const en: typeof tr = {
  common: {
    languageSelector: 'Language selection'
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
  }
};

export type Translations = typeof tr;

export const TRANSLATIONS: Record<Language, Translations> = { tr, en };
