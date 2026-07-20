package com.etiya.cartservice.entities.reference;

import com.etiya.cartservice.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Genel durum referans tablosu (legacy {@code GNL_ST} karşılığı).
 *
 * <p><b>Bounded Context Ownership:</b> Her servis yalnızca kendi dilimine sahiptir.
 * cart-service dilimi: {@code CART}, {@code CART_ITEM}, {@code CART_ITEM_LINE}.
 * Bu tablo cart-service'e sonradan eklenmiştir; iş entity'leri durumlarını buraya
 * {@code general_status_id} FK'si ile bağlar (eskiden {@code isActive} /
 * {@code CartItemStatus} enum'u kullanılıyordu).
 *
 * <p>Kendisi bir durum tablosu olduğundan {@link BaseEntity}'den türer ve durum FK'si
 * taşımaz.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "general_status",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_general_status_entity_short_code",
                columnNames = {"entity_code_name", "short_code"}
        )
)
public class GeneralStatus extends BaseEntity {

    /** Görünen ad (legacy {@code NAME}), ör. "Aktif". */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Açıklama (legacy {@code DESCR}). */
    @Column(name = "description", length = 500)
    private String description;

    /** Stabil iş kodu (legacy {@code SHRT_CODE}), ör. {@code ACTV}. */
    @Column(name = "short_code", nullable = false, length = 50)
    private String shortCode;

    /** Durumun ait olduğu entity'nin kısa kodu (legacy {@code ENT_CODE_NAME}). */
    @Column(name = "entity_code_name", nullable = false, length = 100)
    private String entityCodeName;

    /** Durumun ait olduğu entity'nin adı (legacy {@code ENT_NAME}). */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;
}
