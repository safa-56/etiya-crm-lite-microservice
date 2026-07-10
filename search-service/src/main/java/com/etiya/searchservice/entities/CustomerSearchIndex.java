package com.etiya.searchservice.entities;

import com.etiya.searchservice.entities.enums.CustomerRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Müşteri arama read-model'i (denormalize tek satır = bir müşteri).
 *
 * <p>FR-002 aramasının tüm kriter ve sonuç kolonları bu tek tabloda tutulur.
 * Satırlar cross-service olay akışlarıyla (customer + account) yönetilir
 * (upsert/remove); bu bir CQRS read-model'idir, otoriter kaynak değildir.
 *
 * <p>Bir müşterinin <b>birden çok</b> fatura hesabı olabileceğinden
 * {@code accountNumbers} ve {@code orderNumbers} ayrı koleksiyon tablolarında
 * ({@code search_account_numbers}, {@code search_order_numbers}) tutulur ve arama
 * için indekslenir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "customer_search_index",
        indexes = {
                @Index(name = "ix_search_customer_id", columnList = "customer_id", unique = true),
                @Index(name = "ix_search_nationality_id", columnList = "nationality_id"),
                @Index(name = "ix_search_gsm_number", columnList = "gsm_number"),
                @Index(name = "ix_search_first_name", columnList = "first_name"),
                @Index(name = "ix_search_last_name", columnList = "last_name")
        }
)
public class CustomerSearchIndex extends BaseEntity {

    /** Müşterinin otoriter kimliği (customer-service). Unique iş anahtarı, indeksli. */
    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "second_name", length = 100)
    private String secondName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /** TC kimlik numarası (TCKN) — tam eşleşme kriteri. */
    @Column(name = "nationality_id", length = 11)
    private String nationalityId;

    /** Birincil GSM numarası — tam eşleşme kriteri. */
    @Column(name = "gsm_number", length = 15)
    private String gsmNumber;

    /** Müşteri segmenti/rolü (şimdilik hep B2C). */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private CustomerRole role;

    /** Müşterinin fatura hesabı numaraları (arama kriteri). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "search_account_numbers",
            joinColumns = @JoinColumn(name = "search_index_id"),
            indexes = @Index(name = "ix_search_account_number", columnList = "account_number")
    )
    @Column(name = "account_number", length = 30)
    private Set<String> accountNumbers = new HashSet<>();

    /** Müşterinin fatura hesabı sipariş numaraları (arama kriteri). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "search_order_numbers",
            joinColumns = @JoinColumn(name = "search_index_id"),
            indexes = @Index(name = "ix_search_order_number", columnList = "order_number")
    )
    @Column(name = "order_number", length = 20)
    private Set<String> orderNumbers = new HashSet<>();
}
