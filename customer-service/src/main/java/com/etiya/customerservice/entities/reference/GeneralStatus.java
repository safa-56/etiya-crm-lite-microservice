package com.etiya.customerservice.entities.reference;

import com.etiya.customerservice.entities.BaseEntity;
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
 * <p><b>Bounded context sahipliği:</b> Bu tablo, customer-service'in
 * <i>kendi dilimini</i> tutar; kurum genelindeki tüm durum kodlarını değil.
 * Satırlar {@code entityCodeName} ile bölümlenir ve bu servis yalnızca sahip
 * olduğu entity'lerin ({@code PARTY}, {@code PARTY_ROLE}, {@code IND})
 * satırlarına sahiptir. Diğer servisler ({@code PROD}, {@code CUST_ORD} ...)
 * kendi dilimlerini kendi veritabanlarında tutar; paylaşılan tablo yoktur.
 *
 * <p>Servis sınırını geçen durum bilgisi, {@code id} ile değil <b>{@code shortCode}</b>
 * (ör. {@code ACTV}) ile taşınır; surrogate id bu context'in iç detayıdır.
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

    /** Durumun ait olduğu entity'nin kısa kodu (legacy {@code ENT_CODE_NAME}), ör. {@code PARTY}. */
    @Column(name = "entity_code_name", nullable = false, length = 100)
    private String entityCodeName;

    /** Durumun ait olduğu entity'nin adı (legacy {@code ENT_NAME}), ör. {@code PARTY}. */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;
}
