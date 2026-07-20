package com.etiya.productservice.entities;

import com.etiya.productservice.entities.reference.GeneralStatus;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Durum (status) taşıyan iş entity'lerinin taban sınıfı.
 *
 * <p>{@link BaseEntity}'nin kimlik/audit alanlarına ek olarak, kaydın durumunu
 * {@code general_status} referans tablosuna <b>FK</b> ile bağlar. Eskiden ayrı
 * kolonlarda tutulan {@code isActive} ve entity'ye özel durum enum'ları (ör.
 * {@code ProductStatus}) kaldırılmış; tüm durum bilgisi bu tek FK üzerinden
 * {@link GeneralStatus}'a taşınmıştır.
 *
 * <p>Durum satırı, entity'nin kendi diliminden ({@code entityCodeName}) ve stabil
 * iş kodundan ({@code shortCode}) çözülür; bu çözüm business (manager) katmanında
 * {@code ReferenceDataService} üzerinden yapılır.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class StatusAwareEntity extends BaseEntity {

    /** Kaydın durumu — {@code general_status} referans tablosuna FK. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "general_status_id", nullable = false)
    private GeneralStatus generalStatus;
}
