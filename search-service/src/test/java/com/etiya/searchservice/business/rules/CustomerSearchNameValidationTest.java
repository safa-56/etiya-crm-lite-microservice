package com.etiya.searchservice.business.rules;

import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.core.crosscutting.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ad/soyad kriterlerinin harf doğrulaması: Türkçe karakter, boşluk, kesme işareti ve
 * tire kabul edilir; rakam ve diğer özel karakterler reddedilir.
 */
class CustomerSearchNameValidationTest {

    private final CustomerSearchBusinessRules rules = new CustomerSearchBusinessRules();

    @ParameterizedTest
    @ValueSource(strings = {
            "Ahmet",
            "Şükrü",         // Türkçe karakter
            "İbrahim",       // büyük İ
            "Çağrı",
            "Ömer Faruk",    // boşluk
            "Abdül'aziz",    // düz kesme
            "Abdül’aziz",    // tipografik kesme
            "Ayşe-Nur",      // tire
            "Gülşah Öztürk"
    })
    @DisplayName("Harf, bosluk, kesme ve tire kabul edilir")
    void acceptsLettersAndAllowedPunctuation(String name) {
        assertThatCode(() -> rules.validate(request(name))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Ahmet1",        // rakam
            "123",
            "Ahmet@",        // özel karakter
            "Ahmet_Yilmaz",  // alt çizgi
            "Ahmet.",        // nokta
            "Ahmet+Ayse"
    })
    @DisplayName("Rakam ve diger ozel karakterler reddedilir")
    void rejectsDigitsAndOtherSymbols(String name) {
        assertThatThrownBy(() -> rules.validate(request(name)))
                .isInstanceOf(BusinessException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Yılmaz", "O'Brien", "Kaya-Demir"})
    @DisplayName("Ayni kural soyad alaninda da isler")
    void appliesToLastNameToo(String lastName) {
        assertThatCode(() -> rules.validate(
                new CustomerSearchRequest("B2C", null, null, null, null, null, lastName, null)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> rules.validate(
                new CustomerSearchRequest("B2C", null, null, null, null, null, lastName + "9", null)))
                .isInstanceOf(BusinessException.class);
    }

    /** Yalnızca ad kriteri dolu bir istek; diğer alanlar doğrulamayı etkilemesin diye boştur. */
    private CustomerSearchRequest request(String firstName) {
        return new CustomerSearchRequest("B2C", null, null, null, null, firstName, null, null);
    }
}
