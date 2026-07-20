package com.etiya.accountservice.business.mappers;

import com.etiya.accountservice.business.dtos.requests.CreateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.responses.BillingAccountResponse;
import com.etiya.accountservice.entities.BillingAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * BillingAccount için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>Kimlik/audit alanları ve sistem tarafından atanan {@code accountType},
 * {@code accountStatus}, {@code activeProductCount} mapper'da <b>yok sayılır</b>;
 * bunlar business (manager) katmanında set edilir.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillingAccountMapper {

    // --- request -> entity (yalnızca skaler alanlar; tip/durum manager'da) ---

    @Mapping(target = "accountType", ignore = true)
    @Mapping(target = "generalStatus", ignore = true)
    @Mapping(target = "activeProductCount", ignore = true)
    // Adres metni (snapshot) request'te yok; manager, addressId'yi müşteri
    // projeksiyonundan doğrulayıp adres metnini oradan çözerek set eder.
    @Mapping(target = "address", ignore = true)
    BillingAccount toEntity(CreateBillingAccountRequest request);

    // --- entity -> response ---

    @Mapping(target = "status", source = "generalStatus.shortCode")
    BillingAccountResponse toResponse(BillingAccount entity);
}
