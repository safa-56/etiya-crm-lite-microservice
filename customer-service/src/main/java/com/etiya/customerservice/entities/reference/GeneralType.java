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
 * Genel tip referans tablosu (legacy {@code GNL_TP} karşılığı).
 *
 * <p><b>Bounded context sahipliği:</b> {@link GeneralStatus} ile aynı kural geçerlidir;
 * bu servis yalnızca kendi {@code entityCodeName} dilimine sahiptir. Şu an tek
 * kullanıcısı {@code CAM_PARTY_TYPE} dilimidir (party tipi: bireysel/kurumsal).
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

    /** Görünen ad (legacy {@code NAME}), ör. "Bireysel". */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Açıklama (legacy {@code DESCR}). */
    @Column(name = "description", length = 500)
    private String description;

    /** Stabil iş kodu (legacy {@code SHRT_CODE}), ör. {@code IND}. */
    @Column(name = "short_code", nullable = false, length = 50)
    private String shortCode;

    /** Tipin ait olduğu entity'nin kısa kodu (legacy {@code ENT_CODE_NAME}), ör. {@code CAM_PARTY_TYPE}. */
    @Column(name = "entity_code_name", nullable = false, length = 100)
    private String entityCodeName;

    /**
     * Tipin ait olduğu entity'nin adı (legacy {@code ENT_NAME}).
     *
     * <p><b>Nullable:</b> {@link GeneralStatus}'ün aksine legacy GNL_TP'de bu kolon
     * doldurulmamıştır (dökümdeki tüm satırlarda {@code NULL}); ayrım
     * {@code entityCodeName} üzerinden yapılır. NOT NULL yapmak legacy veriyi
     * yüklenemez hale getirirdi.
     */
    @Column(name = "entity_name", length = 100)
    private String entityName;
}
