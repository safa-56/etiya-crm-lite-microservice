package com.etiya.searchservice.business.dtos.requests;

/**
 * FR-002 müşteri arama kriterleri. Tüm alanlar opsiyoneldir; yalnızca dolu gelen
 * kriterler sorguya katılır.
 *
 * <p>{@code segment} arama segmentidir (varsayılan {@code B2C}). {@code idNumber}
 * TCKN, {@code customerId}/{@code accountNumber}/{@code gsm}/{@code orderNumber}
 * tam-eşleşme; {@code firstName}/{@code lastName} baştan-eşleşme (starts-with)
 * kriterleridir (bkz. {@code CustomerSearchSpecification}).
 */
public record CustomerSearchRequest(
        String segment,
        String idNumber,
        String customerId,
        String accountNumber,
        String gsm,
        String firstName,
        String lastName,
        String orderNumber
) {

    /**
     * First Name / Last Name alanlarının baş/son boşluklarını temizler (ACC-10).
     * Diğer alanlar olduğu gibi bırakılır (tam-eşleşme kriterleri).
     */
    public CustomerSearchRequest withTrimmedNames() {
        return new CustomerSearchRequest(
                segment, idNumber, customerId, accountNumber, gsm,
                trim(firstName), trim(lastName), orderNumber);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /** Redis cache anahtarı — tüm arama parametrelerini içerir. */
    public String cacheKey() {
        return String.join("|",
                nullSafe(segment), nullSafe(idNumber), nullSafe(customerId),
                nullSafe(accountNumber), nullSafe(gsm), nullSafe(firstName),
                nullSafe(lastName), nullSafe(orderNumber));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
