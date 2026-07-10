package com.etiya.searchservice.entities.enums;

/**
 * Müşteri segmenti/rolü — FR-002 arama ekranındaki sol menü segmenti (B2C/B2B).
 *
 * <p>Şu an projede yalnızca bireysel müşteri (B2C) modellenmiştir; {@code B2B}
 * ileride kurumsal müşteri eklendiğinde kullanılmak üzere tanımlıdır.
 */
public enum CustomerRole {
    B2C,
    B2B
}
