package com.etiya.productservice.business.rules;

import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CatalogRepository;
import org.springframework.stereotype.Service;

/**
 * Katalog iş kuralları.
 */
@Service
public class CatalogBusinessRules {

    private final CatalogRepository catalogRepository;

    public CatalogBusinessRules(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    /** Aktif bir katalog id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCatalogExists(Long id) {
        if (!catalogRepository.existsByIdAndDeletedDateIsNull(id)) {
            throw new BusinessException(Messages.CATALOG_NOT_FOUND);
        }
    }
}
