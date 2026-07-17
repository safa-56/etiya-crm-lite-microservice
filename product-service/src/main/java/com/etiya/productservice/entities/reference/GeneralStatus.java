package com.etiya.productservice.entities.reference;

import com.etiya.productservice.entities.BaseEntity;
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
 * <p><b>Bounded Context Ownership:</b> Legacy'de GNL_ST kurum genelinde tek
 * tablodur ve satırlar {@code ENT_CODE_NAME} ile bölümlenir. Mikroserviste bu
 * tabloyu servisler arasında paylaşmak "shared database" anti pattern'i olurdu;
 * bunun yerine her servis <b>yalnızca kendi dilimine</b> sahiptir. product-service
 * dilimi: {@code PROD}, {@code PROD_SPEC}, {@code PROD_CHAR_VAL},
 * {@code PROD_SPEC_SRVC_SPEC}, {@code RSRC_SPEC}.
 *
 * <p>Servis sınırını geçen durum bilgisi {@code id} ile değil {@code shortCode}
 * ile taşınır; surrogate id bu context'in iç detayıdır.
 *
 * <p><b>Mevcut kullanım notu:</b> Bu servisin çalışan durum alanları
 * ({@code Product.status}) bir saga durum makinesidir ve {@code ProductStatus}
 * enum'u ile modellenir; bu tabloya FK vermez. Tablo şu an legacy/ETL hizası ve
 * referans veri altyapısının her serviste tekdüze olması için tutulur.
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

    /** Durumun ait olduğu entity'nin kısa kodu (legacy {@code ENT_CODE_NAME}), ör. {@code PROD}. */
    @Column(name = "entity_code_name", nullable = false, length = 100)
    private String entityCodeName;

    /** Durumun ait olduğu entity'nin adı (legacy {@code ENT_NAME}). */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;
}
