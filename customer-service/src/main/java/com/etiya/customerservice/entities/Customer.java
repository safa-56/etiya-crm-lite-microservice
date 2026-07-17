package com.etiya.customerservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.JOINED)
public class Customer extends BaseEntity {

    /**
     * Müşterinin oynadığı party rolü (legacy {@code CUST.PARTY_ROLE_ID}).
     *
     * <p>{@code CUST -> PARTY_ROLE -> PARTY} zincirini kurar. Bir müşteri tam olarak
     * bir müşteri rolüne karşılık geldiğinden ilişki 1-1'dir.
     *
     * <p><b>Nullable olma sebebi (expand-migrate-contract):</b> Kolon, veri barındıran
     * mevcut {@code customers} tablosuna sonradan eklendi. Dev'de {@code ddl-auto: update}
     * çalıştığından NOT NULL olarak eklenmesi mevcut satırlarda hata verirdi. Yeni
     * kayıtlarda zincirin kurulması business katmanında (IndividualCustomerManager)
     * garanti edilir; eski satırlar {@code data.sql} ile backfill edilir. NOT NULL'a
     * sıkıştırma, backfill tamamlandıktan sonra ayrı bir migration işidir.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_role_id", unique = true)
    private PartyRole partyRole;

    // Cascade yok: çocuk kayıtlar (contactInfos/addresses) manager katmanında
    // kendi repository'leri üzerinden açıkça persist/pasifleştirilir.
    @OneToMany(mappedBy = "customer")
    private List<CustomerContactInfo> contactInfos = new ArrayList<>();

    @OneToMany(mappedBy = "customer")
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
