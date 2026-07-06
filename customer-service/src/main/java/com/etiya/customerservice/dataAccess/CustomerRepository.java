package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Müşteri agrega kökü için repository. Alt tipler (IndividualCustomer) için
 * özel repository'ler ayrıca tanımlıdır; bu repository ortak/genel erişim içindir.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
