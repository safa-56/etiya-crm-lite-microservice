package com.etiya.searchservice.business.rules;

import com.etiya.searchservice.business.constants.Messages;
import com.etiya.searchservice.core.crosscutting.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sayfalama parametrelerinin doğrulaması.
 *
 * <p>Spring'in {@code Pageable} çözücüsü geçersiz {@code page}/{@code size} değerlerini
 * sessizce düzeltip 200 döndürdüğü için, doğrulama ham query değerleri üzerinden yapılır.
 */
class CustomerSearchPaginationValidationTest {

    private final CustomerSearchBusinessRules rules = new CustomerSearchBusinessRules();

    @ParameterizedTest
    @ValueSource(ints = {-1, -50, Integer.MIN_VALUE})
    @DisplayName("Negatif sayfa numarasi reddedilir")
    void rejectsNegativePage(int page) {
        assertThatThrownBy(() -> rules.validatePagination(page, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(Messages.INVALID_PAGE_NUMBER);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 2001, Integer.MAX_VALUE})
    @DisplayName("Sifir, negatif ve ust siniri asan sayfa boyutu reddedilir")
    void rejectsOutOfRangeSize(int size) {
        assertThatThrownBy(() -> rules.validatePagination(null, size))
                .isInstanceOf(BusinessException.class)
                .hasMessage(Messages.INVALID_PAGE_SIZE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100})
    @DisplayName("Gecerli sayfa numarasi kabul edilir")
    void acceptsValidPage(int page) {
        assertThatCode(() -> rules.validatePagination(page, null)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 2000})
    @DisplayName("Gecerli sayfa boyutu kabul edilir")
    void acceptsValidSize(int size) {
        assertThatCode(() -> rules.validatePagination(null, size)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Parametre gonderilmediginde varsayilanlar gecerlidir")
    void acceptsMissingParameters() {
        assertThatCode(() -> rules.validatePagination(null, null)).doesNotThrowAnyException();
    }
}
