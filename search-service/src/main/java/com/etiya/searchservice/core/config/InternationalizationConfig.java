package com.etiya.searchservice.core.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Uluslararasilastirma (i18n) yapilandirmasi.
 *
 * <p>Tum kullaniciya donen mesajlar (validasyon + is kurali) {@code messages*.properties}
 * dosyalarindan, istemcinin {@code Accept-Language} basligina gore secilir.
 * Varsayilan dil Turkce'dir; baslik yoksa ya da desteklenmeyen bir dil istenirse
 * Turkce donulur. Desteklenen diller: Turkce (tr) ve Ingilizce (en).
 */
@Configuration
public class InternationalizationConfig {

    /** Varsayilan dil: Turkce. */
    private static final Locale TURKISH = Locale.forLanguageTag("tr");

    /** Ek desteklenen dil: Ingilizce. */
    private static final Locale ENGLISH = Locale.ENGLISH;

    /**
     * Mesaj kaynagi. {@code messages.properties} (Turkce, temel/varsayilan) ve
     * {@code messages_en.properties} (Ingilizce) paketlerini UTF-8 ile okur.
     * Sistem locale'ine dusme kapalidir; boylece bilinmeyen dil daima temel
     * (Turkce) paketine duser.
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(TURKISH);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * {@code Accept-Language} basligina gore dil cozer. Baslik yok ya da
     * desteklenmiyorsa varsayilan (Turkce) doner.
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(TURKISH);
        resolver.setSupportedLocales(List.of(TURKISH, ENGLISH));
        return resolver;
    }

    /**
     * Bean Validation ({@code @Valid}) mesajlarini yukaridaki {@link MessageSource}
     * uzerinden cozer. Boylece DTO anotasyonlarindaki {@code {anahtar}} ifadeleri
     * istemcinin diline gore Turkce/Ingilizce donusur.
     */
    @Bean
    @Primary
    public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
