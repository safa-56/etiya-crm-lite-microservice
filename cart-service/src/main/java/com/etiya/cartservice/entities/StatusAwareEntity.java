package com.etiya.cartservice.entities;

import com.etiya.cartservice.entities.reference.GeneralStatus;
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
 * kolonlarda tutulan {@code isActive} bayrağı ve {@code CartItemStatus} enum'u
 * kaldırılmış; tüm durum bilgisi bu tek FK üzerinden {@link GeneralStatus}'a
 * taşınmıştır.
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
