package com.etiya.customerservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "street", nullable = false, length = 150)
    private String street;

    @Column(name = "house_number", nullable = false, length = 30)
    private String houseNumber;

    @Column(name = "address_description", nullable = false, length = 500)
    private String addressDescription;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = Boolean.FALSE;
}
