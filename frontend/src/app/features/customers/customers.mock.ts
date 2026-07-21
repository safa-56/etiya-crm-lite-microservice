import { Customer, CustomerAccount } from './customer.model';

/** Sayfalama davranışını göstermek için tekrar eden fatura hesapları üretir. */
function billingAccount(number: string): CustomerAccount {
  return {
    number,
    name: number,
    accountType: 'Billing Account',
    status: 'active',
    products: []
  };
}

/** Tasarım aşaması için örnek veri; ileride müşteri servisiyle değiştirilecek. */
export const MOCK_CUSTOMERS: readonly Customer[] = [
  {
    id: 1100014,
    code: 'C-1100014',
    type: 'B2C',
    firstName: 'Ahmet',
    secondName: 'Kemal',
    lastName: 'Yıldız',
    companyName: null,
    identityNumber: '10293847561',
    gender: 'male',
    birthDate: '1986-07-27',
    motherName: 'Hatice',
    fatherName: 'Ali',
    accountNumber: '0101112911',
    gsm: '5000000000',
    city: 'İstanbul',
    status: 'active',
    registeredAt: '2021-07-12',
    contact: {
      email: 'mmetinkaya@gmail.com',
      mobilePhone: '+90 5000000000',
      homePhone: null,
      fax: null
    },
    addresses: [
      {
        id: 'ADR-1',
        title: 'İstanbul, Atatürk Cd., 14/7',
        detail: 'Bağcılar Cd. No:14, İstanbul',
        isPrimary: false
      },
      {
        id: 'ADR-2',
        title: 'İzmir, Karşıyaka, 220/3',
        detail: 'Bostanlı Mah. Cemal Gürsel Cad. No:220, İzmir',
        isPrimary: true
      }
    ],
    accounts: [
      {
        number: '0101112911',
        name: '0101112911',
        accountType: 'Billing Account',
        status: 'active',
        products: [
          { id: '001', name: 'ADSL 8MB', campaignName: 'Summer Fiber', campaignId: '1001' },
          { id: '002', name: 'ADSL Data Medium', campaignName: null, campaignId: null }
        ]
      },
      {
        number: '0101112915',
        name: '0101112915',
        accountType: 'Billing Account',
        status: 'active',
        products: [
          { id: '003', name: 'Fiber 100 Mbps', campaignName: 'Fiber Fırsatı', campaignId: '1042' }
        ]
      },
      billingAccount('0101112441'),
      billingAccount('0103452911'),
      {
        number: '0103452915',
        name: '0103452915',
        accountType: 'Billing Account',
        status: 'active',
        products: [
          { id: '004', name: 'Mobil Hat 20 GB', campaignName: 'Yaz Paketi', campaignId: '1105' }
        ]
      },
      billingAccount('0103452441'),
      billingAccount('0105112911'),
      {
        number: '0105112915',
        name: '0105112915',
        accountType: 'Billing Account',
        status: 'passive',
        products: []
      },
      billingAccount('0105112441'),
      billingAccount('0107452911')
    ],
    orders: [
      {
        orderNumber: '90014582',
        date: '2026-05-11',
        product: 'Fiber İnternet 100 Mbps',
        amount: 749.9,
        status: 'completed'
      },
      {
        orderNumber: '90014901',
        date: '2026-06-02',
        product: 'Mobil Hat Taşıma',
        amount: 249,
        status: 'pending'
      }
    ]
  },
  {
    id: 1100387,
    code: 'C-1100387',
    type: 'B2C',
    firstName: 'Ayşe',
    secondName: null,
    lastName: 'Yılmaz',
    companyName: null,
    identityNumber: '12345678901',
    gender: 'female',
    birthDate: '1990-02-08',
    motherName: 'Sevim',
    fatherName: 'Hasan',
    accountNumber: '3300124578',
    gsm: '5321234567',
    city: 'İstanbul',
    status: 'active',
    registeredAt: '2021-03-14',
    contact: {
      email: 'ayse.yilmaz@example.com',
      mobilePhone: '+90 5321234567',
      homePhone: '+90 2163334455',
      fax: null
    },
    addresses: [
      {
        id: 'ADR-3',
        title: 'İstanbul, Moda Cd., 14/5',
        detail: 'Caferağa Mah. Moda Cad. No:14 D:5, Kadıköy',
        isPrimary: true
      }
    ],
    accounts: [
      {
        number: '3300124578',
        name: '3300124578',
        accountType: 'Billing Account',
        status: 'active',
        products: [
          { id: '011', name: 'Fiber 100 Mbps', campaignName: 'Summer Fiber', campaignId: '1001' }
        ]
      },
      billingAccount('3300124590')
    ],
    orders: [
      {
        orderNumber: '90015220',
        date: '2026-04-27',
        product: 'TV+ Paketi',
        amount: 189.5,
        status: 'completed'
      }
    ]
  },
  {
    id: 1100455,
    code: 'C-1100455',
    type: 'B2C',
    firstName: 'Mehmet',
    secondName: null,
    lastName: 'Demir',
    companyName: null,
    identityNumber: '23456789012',
    gender: 'male',
    birthDate: '1978-11-30',
    motherName: 'Fatma',
    fatherName: 'Osman',
    accountNumber: '3300987412',
    gsm: '5449876543',
    city: 'Ankara',
    status: 'active',
    registeredAt: '2022-11-02',
    contact: {
      email: 'mehmet.demir@example.com',
      mobilePhone: '+90 5449876543',
      homePhone: null,
      fax: null
    },
    addresses: [
      {
        id: 'ADR-4',
        title: 'Ankara, Atatürk Blv., 88',
        detail: 'Kızılay Mah. Atatürk Bulvarı No:88, Çankaya',
        isPrimary: true
      }
    ],
    accounts: [billingAccount('3300987412')],
    orders: []
  },
  {
    id: 1100512,
    code: 'C-1100512',
    type: 'B2C',
    firstName: 'Zeynep',
    secondName: 'Nur',
    lastName: 'Kara',
    companyName: null,
    identityNumber: '34567890123',
    gender: 'female',
    birthDate: '1995-06-19',
    motherName: 'Emine',
    fatherName: 'Kemal',
    accountNumber: '3300556677',
    gsm: '5335554433',
    city: 'İzmir',
    status: 'passive',
    registeredAt: '2020-06-19',
    contact: {
      email: 'zeynep.kara@example.com',
      mobilePhone: '+90 5335554433',
      homePhone: null,
      fax: null
    },
    addresses: [],
    accounts: [],
    orders: []
  }
];
