package com.etiya.cartservice.dataAccess;

import com.etiya.cartservice.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Sepet satırı (CartItem) veri erişimi.
 *
 * <p>Yalnızca aktif (silinmemiş) satırlar üzerinden çalışan türetilmiş sorgular
 * sağlar; sepet toplamı ve tekrar kontrolü bunlara dayanır.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Bir sepetin tüm aktif satırlarını getirir. */
    List<CartItem> findAllByCartIdAndDeletedDateIsNull(Long cartId);

    /** Sepette belirli bir teklife ait aktif satırı getirir (adet artırımı için). */
    Optional<CartItem> findByCartIdAndProductOfferIdAndDeletedDateIsNull(Long cartId, Long productOfferId);

    /** Sepette belirli bir kampanyaya ait aktif satır zaten var mı? */
    boolean existsByCartIdAndCampaignIdAndDeletedDateIsNull(Long cartId, Long campaignId);

    /** Sepetteki bir satırı id + sepet ile aktif olarak getirir. */
    Optional<CartItem> findByIdAndCartIdAndDeletedDateIsNull(Long id, Long cartId);
}
