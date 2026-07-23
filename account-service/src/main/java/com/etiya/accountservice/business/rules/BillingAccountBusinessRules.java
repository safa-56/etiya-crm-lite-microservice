package com.etiya.accountservice.business.rules;

import com.etiya.accountservice.business.constants.Messages;
import com.etiya.accountservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.accountservice.dataAccess.BillingAccountRepository;
import com.etiya.accountservice.entities.BillingAccount;
import org.springframework.stereotype.Service;

/**
 * Fatura hesabı iş kuralları.
 *
 * <p>İş katmanındaki kontrolleri (validation'dan farklı olarak veri/durum
 * bağımlı kurallar) burada toplar. İlgili business sınıfına (manager) inject
 * edilir; manager kuralları çağırarak akışı yönetir. Kural ihlallerinde
 * {@link BusinessException} fırlatılır (mesajlar {@link Messages} sabitlerinden).
 */
@Service
public class BillingAccountBusinessRules {

    private final BillingAccountRepository billingAccountRepository;

    public BillingAccountBusinessRules(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    /** Aktif bir fatura hesabı id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfBillingAccountExists(Long id) {
        if (!billingAccountRepository.existsByIdAndDeletedDateIsNull(id)) {
            throw new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND);
        }
    }

    /**
     * Verilen hesap numarası daha önce kullanılmışsa hata verir.
     *
     * <p>Kontrol soft-delete edilmiş hesapları da kapsar: numara kalıcı bir iş
     * kimliğidir, iptal edilmiş bir hesabınki yeniden verilemez. Aynı kural
     * veritabanında unique kısıtla da zorlanır; buradaki kontrol kullanıcıya
     * okunabilir bir mesaj dönebilmek içindir.
     */
    public void checkIfAccountNumberAlreadyExists(String accountNumber) {
        if (accountNumber != null && !accountNumber.isBlank()
                && billingAccountRepository.existsByAccountNumber(accountNumber)) {
            throw new BusinessException(Messages.ACCOUNT_NUMBER_ALREADY_EXISTS);
        }
    }

    /**
     * Silme öncesi kontrol: hesaba bağlı en az bir aktif ürün varsa hesap
     * silinemez; kabul kriteri gereği birebir tanımlı mesaj fırlatılır.
     */
    public void checkIfBillingAccountHasNoActiveProducts(BillingAccount billingAccount) {
        Integer activeProductCount = billingAccount.getActiveProductCount();
        if (activeProductCount != null && activeProductCount > 0) {
            throw new BusinessException(Messages.BILLING_ACCOUNT_HAS_ACTIVE_PRODUCTS);
        }
    }
}
