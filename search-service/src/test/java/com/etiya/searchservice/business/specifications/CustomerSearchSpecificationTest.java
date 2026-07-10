package com.etiya.searchservice.business.specifications;

import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.dataAccess.CustomerSearchIndexRepository;
import com.etiya.searchservice.entities.CustomerSearchIndex;
import com.etiya.searchservice.entities.enums.CustomerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FR-002 arama Specification'ının hermetik (H2) testi: tam-eşleşme vs starts-with,
 * ACC-15 AND, ACC-16 OR, segment filtresi, sıralama (customerId asc) ve sayfalama.
 */
@SpringBootTest
@ActiveProfiles("test")
class CustomerSearchSpecificationTest {

    private static final Pageable FIRST_PAGE =
            PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "customerId"));

    @Autowired
    private CustomerSearchIndexRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        // 1001 Ahmet Yilmaz — hesap/sipariş numaralı
        repository.save(row(1001L, "Ahmet", "Yilmaz", "11111111111", "5550001",
                CustomerRole.B2C, Set.of("ACC-100"), Set.of("ORD-1")));
        // 1002 Ayse Yildiz
        repository.save(row(1002L, "Ayse", "Yildiz", "22222222222", "5550002",
                CustomerRole.B2C, Set.of(), Set.of()));
        // 1003 Mehmet Yilmaz
        repository.save(row(1003L, "Mehmet", "Yilmaz", "33333333333", "5550003",
                CustomerRole.B2C, Set.of(), Set.of()));
        // 1004 Zeynep Kaya — B2B (B2C aramasında görünmemeli)
        repository.save(row(1004L, "Zeynep", "Kaya", "44444444444", "5550004",
                CustomerRole.B2B, Set.of(), Set.of()));
        repository.flush();
    }

    @Test
    void exactMatch_idNumber_returnsOnlyMatchingCustomer() {
        var request = request().idNumber("11111111111").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1001L);
    }

    @Test
    void exactMatch_customerId_returnsOnlyMatchingCustomer() {
        var request = request().customerId("1002").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1002L);
    }

    @Test
    void startsWith_firstName_isCaseInsensitive() {
        // "ah" (küçük harf) → Ahmet (baştan eşleşme, case-insensitive — ACC-17).
        var request = request().firstName("ah").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1001L);
    }

    @Test
    void firstAndLastName_areAndedTogether() {
        // firstName "a" + lastName "yild" → yalnızca Ayse Yildiz (ACC-15 AND).
        var request = request().firstName("a").lastName("yild").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1002L);
    }

    @Test
    void nameBlock_orsWithOtherCriteria() {
        // (firstName "ayse") OR (idNumber 33333333333) → 1002 ve 1003 (ACC-16 OR).
        var request = request().firstName("ayse").idNumber("33333333333").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactlyInAnyOrder(1002L, 1003L);
    }

    @Test
    void accountNumber_exactMatch_inChildCollection() {
        var request = request().accountNumber("ACC-100").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1001L);
    }

    @Test
    void orderNumber_exactMatch_inChildCollection() {
        var request = request().orderNumber("ORD-1").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1001L);
    }

    @Test
    void b2cSegment_excludesB2bRows_andSortsByCustomerIdAsc() {
        // Kriter yok → segment (B2C) içindeki tüm satırlar, customerId artan (ACC-20).
        var request = request().build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1001L, 1002L, 1003L); // 1004 (B2B) hariç, sıralı
    }

    @Test
    void b2bSegment_returnsOnlyB2bRows() {
        var request = request().segment("B2B").firstName("zeynep").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1004L);
    }

    @Test
    void noMatch_returnsEmpty() {
        var request = request().idNumber("99999999999").build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), FIRST_PAGE);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void paging_firstPageIsCappedBySize() {
        // Sayfa boyutu 2 → ilk sayfada 2 kayıt, son sayfa değil (ACC-19 sayfalama).
        Pageable smallPage = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "customerId"));
        var request = request().build();
        Page<CustomerSearchIndex> result = repository.findAll(CustomerSearchSpecification.build(request), smallPage);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3); // B2C toplam
        assertThat(result.isLast()).isFalse();
        assertThat(result.getContent()).extracting(CustomerSearchIndex::getCustomerId)
                .containsExactly(1001L, 1002L);
    }

    // ------------------------------------------------------------------ yardımcılar

    private CustomerSearchIndex row(Long customerId, String firstName, String lastName,
                                    String nationalityId, String gsm, CustomerRole role,
                                    Set<String> accountNumbers, Set<String> orderNumbers) {
        CustomerSearchIndex index = new CustomerSearchIndex();
        index.setCustomerId(customerId);
        index.setFirstName(firstName);
        index.setLastName(lastName);
        index.setNationalityId(nationalityId);
        index.setGsmNumber(gsm);
        index.setRole(role);
        index.setIsActive(true);
        index.getAccountNumbers().addAll(accountNumbers);
        index.getOrderNumbers().addAll(orderNumbers);
        return index;
    }

    private RequestBuilder request() {
        return new RequestBuilder();
    }

    /** Test okunabilirliği için küçük bir arama isteği kurucusu. */
    private static final class RequestBuilder {
        private String segment = "B2C";
        private String idNumber;
        private String customerId;
        private String accountNumber;
        private String gsm;
        private String firstName;
        private String lastName;
        private String orderNumber;

        RequestBuilder segment(String v) { this.segment = v; return this; }
        RequestBuilder idNumber(String v) { this.idNumber = v; return this; }
        RequestBuilder customerId(String v) { this.customerId = v; return this; }
        RequestBuilder accountNumber(String v) { this.accountNumber = v; return this; }
        RequestBuilder gsm(String v) { this.gsm = v; return this; }
        RequestBuilder firstName(String v) { this.firstName = v; return this; }
        RequestBuilder lastName(String v) { this.lastName = v; return this; }
        RequestBuilder orderNumber(String v) { this.orderNumber = v; return this; }

        CustomerSearchRequest build() {
            return new CustomerSearchRequest(
                    segment, idNumber, customerId, accountNumber, gsm, firstName, lastName, orderNumber);
        }
    }
}
