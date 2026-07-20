package com.etiya.productservice.entities;

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
 * Tüm entity'lerin miras aldığı taban sınıf — ortak kimlik ve denetim (audit) alanları.
 *
 * <p><b>Durum (status) alanı burada tutulmaz.</b> Eskiden bu sınıfta bulunan
 * {@code isActive} bayrağı kaldırılmıştır; bir kaydın aktif/pasif/iptal/beklemede
 * gibi <i>durum</i> bilgisi artık {@code general_status} referans tablosuna FK ile
 * taşınır (bkz. {@link StatusAwareEntity}). Böylece "durum bilgisi yalnızca
 * general_status'ta tutulur" kuralı sağlanır.
 *
 * <p>Referans tabloları ({@code general_status}, {@code general_type}) kendileri
 * durum taşımadığından doğrudan bu sınıftan türer; iş entity'leri ise durum FK'sini
 * ekleyen {@link StatusAwareEntity}'den türer. {@code deletedDate} bir denetim
 * zaman damgasıdır (durum alanı değildir) ve soft-delete filtrelerinde kullanılır.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
