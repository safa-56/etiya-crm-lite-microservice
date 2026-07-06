package com.etiya.customerservice.entities;

import com.etiya.customerservice.entities.enums.GenderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Bireysel müşteri (IndividualCustomer) entity'si.
 *
 * <p>{@link Customer}'ı miras alır (JOINED). ER modelindeki
 * {@code IndividualCustomers} tablosuna karşılık gelir ve {@code customer_id}
 * ({@link PrimaryKeyJoinColumn}) ile {@code customers.id}'ye bağlanır (1-1).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "individual_customers")
@PrimaryKeyJoinColumn(name = "customer_id")
public class IndividualCustomer extends Customer {

    /** Cinsiyet referans kimliği (gender lookup tablosuna FK). */
    @Column(name = "gender_id")
    private Long genderId;

    /** Uyruk referans kimliği (nationality lookup). */
    @Column(name = "nationality_id")
    private Long nationalityId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "second_name", length = 100)
    private String secondName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_type", length = 20)
    private GenderType genderType;
}
