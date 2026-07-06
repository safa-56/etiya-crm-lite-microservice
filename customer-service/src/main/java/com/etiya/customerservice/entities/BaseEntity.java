package com.etiya.customerservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tüm entity'lerin miras aldığı taban sınıf.
 *
 * <p>Ortak alanları ve denetim (audit) davranışını tek yerde toplar:
 * <ul>
 *   <li>{@code id}          - birincil anahtar</li>
 *   <li>{@code createdDate} - kayıt oluşturulma zamanı</li>
 *   <li>{@code updatedDate} - son güncelleme zamanı</li>
 *   <li>{@code deletedDate} - soft-delete zamanı (silinmediyse {@code null})</li>
 *   <li>{@code isActive}    - kayıt aktif mi (soft-delete bayrağı)</li>
 * </ul>
 *
 * <p>{@link MappedSuperclass} olduğu için kendi tablosu yoktur; alanları miras
 * alan entity'nin (JOINED stratejisinde kök) tablosuna iner. Zaman damgaları
 * JPA yaşam döngüsü geri çağrıları ({@link PrePersist}/{@link PreUpdate}) ile
 * otomatik doldurulur.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** Yeni kayıt: oluşturulma zamanını ata, aktiflik verilmediyse {@code true} yap. */
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = Boolean.TRUE;
        }
    }

    /** Güncelleme: güncelleme zamanını tazele. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
