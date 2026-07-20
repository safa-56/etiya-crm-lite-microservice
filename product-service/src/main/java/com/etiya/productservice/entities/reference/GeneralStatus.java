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
 * <p><b>Kullanım:</b> Bu servisin iş entity'leri (Product, ProductOffer,
 * ProductSpec, Catalog, Campaign, CampaignOffer) durumlarını artık bu tabloya
 * <b>FK</b> ile ({@code general_status_id}) bağlar; entity'lerde ayrı bir durum
 * kolonu (isActive / status enum'u) tutulmaz. Ürün satış saga'sı da durumu
 * ({@code PNDG} → {@code ACTV}/{@code QUOTE_DEL}) bu tablodaki satırlarla yürütür.
 *
 * <p>Kendisi bir durum tablosu olduğundan {@link BaseEntity}'den türer ve durum
 * FK'si taşımaz (kendine referans vermez).
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
