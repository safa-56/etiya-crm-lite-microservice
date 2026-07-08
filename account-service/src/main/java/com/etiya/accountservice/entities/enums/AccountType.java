package com.etiya.accountservice.entities.enums;

/**
 * Hesap tipi.
 *
 * <p>Bu serviste oluşturulan her hesap bir fatura hesabıdır; yeni kayıtlarda
 * {@link #BILLING_ACCOUNT} olarak atanır (kabul kriteri: "Account Type =
 * Billing Account").
 */
public enum AccountType {

    /** Fatura hesabı — ekranlarda "Billing Account" olarak gösterilir. */
    BILLING_ACCOUNT
}
