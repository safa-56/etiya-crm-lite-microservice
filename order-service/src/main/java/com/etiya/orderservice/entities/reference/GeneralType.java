package com.etiya.orderservice.entities.reference;

import com.etiya.orderservice.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Genel tip referans tablosu (legacy {@code GNL_TP} karşılığı).
 *
 * <p><b>Bounded Context Ownership:</b> {@link GeneralStatus} ile aynı kural
 * geçerlidir; bu servis yalnızca kendi {@code entityCodeName} dilimine sahiptir.
 *
 * <p><b>Seed durumu:</b> Legacy GNL_TP dökümü henüz elimizde olmadığından bu
 * tablo şu an tohumlanmamıştır (bkz. {@code data.sql}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "general_type",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_general_type_entity_short_code",
                columnNames = {"entity_code_name", "short_code"}
        )
)
public class GeneralType extends BaseEntity {

    /** Görünen ad (legacy {@code NAME}). */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Açıklama (legacy {@code DESCR}). */
    @Column(name = "description", length = 500)
    private String description;

    /** Stabil iş kodu (legacy {@code SHRT_CODE}). */
    @Column(name = "short_code", nullable = false, length = 50)
    private String shortCode;

    /** Tipin ait olduğu entity'nin kısa kodu (legacy {@code ENT_CODE_NAME}). */
    @Column(name = "entity_code_name", nullable = false, length = 100)
    private String entityCodeName;

    /**
     * Tipin ait olduğu entity'nin adı (legacy {@code ENT_NAME}).
     *
     * <p><b>Nullable:</b> {@link GeneralStatus}'ün aksine legacy GNL_TP'de bu kolon
     * doldurulmamıştır; ayrım {@code entityCodeName} üzerinden yapılır.
     */
    @Column(name = "entity_name", length = 100)
    private String entityName;
}
