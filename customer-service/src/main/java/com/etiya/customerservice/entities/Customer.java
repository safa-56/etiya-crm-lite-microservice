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
