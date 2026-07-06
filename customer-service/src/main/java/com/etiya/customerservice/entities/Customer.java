package com.etiya.customerservice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Ana müşteri (Customer) entity'si — müşteri agregasının köküdür.
 *
 * <p>ER modelinde {@code Customers} tablosuna karşılık gelir. Müşteri tipleri
 * (ör. {@link IndividualCustomer}) bu sınıfı miras alır. JPA'da
 * {@link InheritanceType#JOINED} stratejisi kullanılır: alt tip kendi tablosunda
 * yalnızca ek alanlarını tutar ve {@code customer_id} ile bu tablonun
 * birincil anahtarını paylaşır (görseldeki 1-1 ilişki).
 *
 * <p>Ortak alanlar (id, created_date, updated_date, deleted_date, is_active)
 * {@link BaseEntity}'den gelir ve {@code customers} tablosunda tutulur.
 * (Görseldeki {@code is_deleted}, BaseEntity'nin {@code is_active} +
 * {@code deleted_date} soft-delete alanlarıyla karşılanır.)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.JOINED)
public class Customer extends BaseEntity {

    /**
     * Müşterinin iletişim bilgileri (1-N). {@code CustomerContactInfo.customer}
     * tarafından yönetilir. Agrega kökü üzerinden cascade edilir.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerContactInfo> contactInfos = new ArrayList<>();

    /**
     * Müşterinin adresleri (1-N). {@code Address.customer} tarafından yönetilir.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    /** İki yönlü ilişkiyi tutarlı kurmak için yardımcı. */
    public void addContactInfo(CustomerContactInfo contactInfo) {
        contactInfos.add(contactInfo);
        contactInfo.setCustomer(this);
    }

    /** İki yönlü ilişkiyi tutarlı kurmak için yardımcı. */
    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }
}
