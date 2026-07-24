package com.etiya.searchservice.business.rules;

import com.etiya.searchservice.business.constants.Messages;
import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.core.crosscutting.exceptions.BusinessException;
import org.springframework.stereotype.Service;

/**
 * FR-002 arama parametrelerinin backend format doğrulaması (ACC-04..10).
 *
 * <p>Yalnızca DOLU gelen alanlar doğrulanır (tümü opsiyonel). İhlalde
 * {@link BusinessException} fırlatılır; {@code GlobalExceptionHandler} bunu 400'e
 * çevirir. First Name / Last Name trim'i çağırandan önce yapılmış varsayılır
 * (ACC-10 — bkz. {@link CustomerSearchRequest#withTrimmedNames()}).
 */
@Service
public class CustomerSearchBusinessRules {

    private static final int MAX_GSM_LENGTH = 15;
    private static final int MAX_CUSTOMER_ID_LENGTH = 20;
    /** Hesap numarası account-service'te tam 10 haneli ve yalnızca rakamdır. */
    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    /** Sipariş numarası order-service'te tam 8 haneli ve yalnızca rakamdır. */
    private static final int ORDER_NUMBER_LENGTH = 8;
    private static final int MAX_NAME_LENGTH = 50;
    /**
     * Tek istekte istenebilecek en fazla kayıt sayısı. Spring'in
     * {@code spring.data.web.pageable.max-page-size} varsayılanı ile aynıdır; ikisi
     * ayrışırsa büyük {@code size} değerleri hata yerine yine sessizce kırpılır.
     */
    private static final int MAX_PAGE_SIZE = 2000;

    /**
     * Ad/soyad: Türkçe dâhil harfler, boşluk, kesme işareti ve tire. Rakam ve diğer
     * özel karakterler kabul edilmez. customer-service'teki
     * {@code ValidationPatterns.NAME_PATTERN} ile aynı kümedir; biri değişirse diğeri de
     * değişmelidir. Burada nicelik belirteci {@code +}'dır çünkü boş kriter zaten
     * {@code isPresent} ile elenir.
     */
    private static final String NAME_PATTERN = "[A-Za-zÇçĞğİıÖöŞşÜüÂâÎîÛû '’-]+";

    /** Tüm dolu kriterleri format kurallarına göre doğrular. */
    public void validate(CustomerSearchRequest request) {
        // ACC-04: ID Number (TCKN) yalnızca rakam, tam 11 hane.
        if (isPresent(request.idNumber()) && !request.idNumber().matches("\\d{11}")) {
            throw new BusinessException(Messages.INVALID_ID_NUMBER);
        }
        // ACC-05: GSM yalnızca rakam, en fazla 15 karakter.
        if (isPresent(request.gsm())
                && !request.gsm().matches("\\d{1," + MAX_GSM_LENGTH + "}")) {
            throw new BusinessException(Messages.INVALID_GSM_NUMBER);
        }
        // ACC-06: Customer ID yalnızca rakam, en fazla 20 karakter.
        if (isPresent(request.customerId())
                && !request.customerId().matches("\\d{1," + MAX_CUSTOMER_ID_LENGTH + "}")) {
            throw new BusinessException(Messages.INVALID_CUSTOMER_ID);
        }
        // ACC-07: Account Number yalnızca rakam, tam 10 hane (account-service ile aynı
        // kural). Arama tam eşleşme yaptığından kısmi numarayla arama yapılamaz.
        if (isPresent(request.accountNumber())
                && !request.accountNumber().matches("\\d{" + ACCOUNT_NUMBER_LENGTH + "}")) {
            throw new BusinessException(Messages.INVALID_ACCOUNT_NUMBER);
        }
        // ACC-08: Order Number yalnızca rakam, tam 8 hane (order-service ile aynı kural).
        if (isPresent(request.orderNumber())
                && !request.orderNumber().matches("\\d{" + ORDER_NUMBER_LENGTH + "}")) {
            throw new BusinessException(Messages.INVALID_ORDER_NUMBER);
        }
        // ACC-09: First Name / Last Name en fazla 50 karakter.
        if (exceedsLength(request.firstName()) || exceedsLength(request.lastName())) {
            throw new BusinessException(Messages.INVALID_NAME_LENGTH);
        }
        // Ad/soyad yalnızca harf, boşluk, kesme işareti ve tire içerebilir
        // (customer-service'teki ValidationPatterns.NAME_PATTERN ile aynı küme).
        if (violatesNamePattern(request.firstName()) || violatesNamePattern(request.lastName())) {
            throw new BusinessException(Messages.INVALID_NAME_PATTERN);
        }
    }

    /**
     * Sayfalama parametrelerini doğrular. <b>Ham</b> query değerleriyle çağrılmalıdır:
     * Spring'in {@code Pageable} çözücüsü negatif {@code page}'i 0'a, {@code size <= 1}
     * değerlerini varsayılana ve üst sınırı aşan {@code size}'ı {@link #MAX_PAGE_SIZE}'a
     * sessizce sabitler; dolayısıyla {@code Pageable} nesnesine bakarak geçersiz girdi
     * anlaşılamaz.
     *
     * @param page ham {@code page} parametresi ({@code null} = gönderilmemiş, varsayılan kullanılır)
     * @param size ham {@code size} parametresi ({@code null} = gönderilmemiş, varsayılan kullanılır)
     */
    public void validatePagination(Integer page, Integer size) {
        if (page != null && page < 0) {
            throw new BusinessException(Messages.INVALID_PAGE_NUMBER);
        }
        if (size != null && (size < 1 || size > MAX_PAGE_SIZE)) {
            // Sayı MessageFormat'ta binlik ayraç almasın diye metin olarak geçilir.
            throw new BusinessException(Messages.INVALID_PAGE_SIZE, String.valueOf(MAX_PAGE_SIZE));
        }
    }

    private boolean violatesNamePattern(String value) {
        return isPresent(value) && !value.matches(NAME_PATTERN);
    }

    private boolean exceedsLength(String value) {
        return value != null && value.length() > MAX_NAME_LENGTH;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
