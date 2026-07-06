package com.etiya.customerservice.entities.enums;

/**
 * Cinsiyet türü. {@code gender_type} alanı için kullanılır ve veritabanında
 * (magic string yerine) enum adı olarak {@code STRING} biçiminde saklanır.
 */
public enum GenderType {
    MALE,
    FEMALE,
    OTHER,
    UNKNOWN
}
