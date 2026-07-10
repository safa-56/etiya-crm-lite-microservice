package com.etiya.searchservice.business.specifications;

import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.entities.CustomerSearchIndex;
import com.etiya.searchservice.entities.enums.CustomerRole;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * FR-002 dinamik müşteri arama sorgusunu JPA Criteria API ile kuran Specification.
 *
 * <p>Kurallar:
 * <ul>
 *   <li><b>segment</b> (ACC-01): yalnızca ilgili role (varsayılan {@code B2C}) satırları;
 *       her zaman AND ile uygulanır. Ayrıca yalnızca aktif ({@code is_active}) satırlar.</li>
 *   <li><b>ACC-14 (tam eşleşme):</b> {@code customerId}, {@code idNumber} (TCKN),
 *       {@code accountNumber}, {@code gsm}, {@code orderNumber}.</li>
 *   <li><b>ACC-17 (starts-with, case-insensitive):</b> {@code firstName}, {@code lastName}
 *       ({@code ILIKE 'değer%'}).</li>
 *   <li><b>ACC-15 (AND):</b> {@code firstName} + {@code lastName} birlikte verildiyse AND.</li>
 *   <li><b>ACC-16 (OR):</b> (isim bloğu) İLE diğer tüm kriterler arasında OR. Yalnızca
 *       DOLU gelen kriterler bloğa eklenir.</li>
 * </ul>
 *
 * <p>{@code accountNumber}/{@code orderNumber} çocuk koleksiyonlarda arandığından
 * OR-güvenli bir <b>EXISTS alt sorgusu</b> ile eşleştirilir (join çoğullaması / satır
 * tekrarı olmaz; sayfalama temiz kalır).
 */
public final class CustomerSearchSpecification {

    private CustomerSearchSpecification() {
    }

    public static Specification<CustomerSearchIndex> build(CustomerSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> mandatory = new ArrayList<>();

            // --- her zaman: yalnızca aktif satırlar ---
            mandatory.add(cb.isTrue(root.get("isActive")));

            // --- segment (ACC-01): role eşleşmesi (AND) ---
            mandatory.add(cb.equal(root.get("role"), resolveSegment(request.segment())));

            // --- OR bloğu (ACC-16): yalnızca dolu kriterler ---
            List<Predicate> orBlock = new ArrayList<>();

            // İsim bloğu (ACC-15 AND + ACC-17 starts-with, case-insensitive)
            Predicate namePredicate = buildNamePredicate(root, cb, request);
            if (namePredicate != null) {
                orBlock.add(namePredicate);
            }

            // Tam eşleşme kriterleri (ACC-14)
            if (isPresent(request.customerId())) {
                orBlock.add(customerIdEquals(root, cb, request.customerId()));
            }
            if (isPresent(request.idNumber())) {
                orBlock.add(cb.equal(root.get("nationalityId"), request.idNumber()));
            }
            if (isPresent(request.gsm())) {
                orBlock.add(cb.equal(root.get("gsmNumber"), request.gsm()));
            }
            if (isPresent(request.accountNumber())) {
                orBlock.add(collectionContains(query, cb, root, "accountNumbers", request.accountNumber()));
            }
            if (isPresent(request.orderNumber())) {
                orBlock.add(collectionContains(query, cb, root, "orderNumbers", request.orderNumber()));
            }

            if (!orBlock.isEmpty()) {
                mandatory.add(cb.or(orBlock.toArray(new Predicate[0])));
            }

            return cb.and(mandatory.toArray(new Predicate[0]));
        };
    }

    /**
     * First Name / Last Name için starts-with (case-insensitive) predicate; ikisi de
     * verildiyse AND'lenir. Hiçbiri yoksa {@code null}.
     */
    private static Predicate buildNamePredicate(Root<CustomerSearchIndex> root,
                                                CriteriaBuilder cb,
                                                CustomerSearchRequest request) {
        List<Predicate> names = new ArrayList<>();
        if (isPresent(request.firstName())) {
            names.add(startsWithIgnoreCase(root, cb, "firstName", request.firstName()));
        }
        if (isPresent(request.lastName())) {
            names.add(startsWithIgnoreCase(root, cb, "lastName", request.lastName()));
        }
        if (names.isEmpty()) {
            return null;
        }
        return cb.and(names.toArray(new Predicate[0]));
    }

    private static Predicate startsWithIgnoreCase(Root<CustomerSearchIndex> root,
                                                  CriteriaBuilder cb,
                                                  String attribute,
                                                  String value) {
        return cb.like(cb.lower(root.get(attribute)), value.toLowerCase() + "%");
    }

    /**
     * customerId tam eşleşmesi. Değer 20 haneye kadar rakam olabildiğinden Long
     * aralığını aşabilir; parse edilemezse hiçbir satırla eşleşmeyen bir predicate
     * döner (OR'a katkı vermez).
     */
    private static Predicate customerIdEquals(Root<CustomerSearchIndex> root,
                                              CriteriaBuilder cb,
                                              String value) {
        try {
            return cb.equal(root.get("customerId"), Long.parseLong(value));
        } catch (NumberFormatException e) {
            return cb.disjunction(); // her zaman false
        }
    }

    /**
     * Bir müşterinin çocuk koleksiyonunda ({@code accountNumbers}/{@code orderNumbers})
     * verilen değerin bulunup bulunmadığını OR-güvenli biçimde kontrol eder (EXISTS
     * alt sorgusu — ana sorguya join eklemez).
     */
    private static Predicate collectionContains(CriteriaQuery<?> query,
                                                CriteriaBuilder cb,
                                                Root<CustomerSearchIndex> root,
                                                String collectionAttribute,
                                                String value) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<CustomerSearchIndex> subRoot = sub.from(CustomerSearchIndex.class);
        sub.select(subRoot.get("id"));
        sub.where(
                cb.equal(subRoot.get("id"), root.get("id")),
                cb.equal(subRoot.join(collectionAttribute), value));
        return cb.exists(sub);
    }

    /** Geçersiz/boş segment {@code B2C}'ye düşer (varsayılan segment). */
    private static CustomerRole resolveSegment(String segment) {
        if (segment == null || segment.isBlank()) {
            return CustomerRole.B2C;
        }
        try {
            return CustomerRole.valueOf(segment.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CustomerRole.B2C;
        }
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
