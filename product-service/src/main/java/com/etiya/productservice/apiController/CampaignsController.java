package com.etiya.productservice.apiController;

import com.etiya.productservice.business.abstracts.CampaignService;
import com.etiya.productservice.business.dtos.requests.CreateCampaignRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCampaignRequest;
import com.etiya.productservice.business.dtos.responses.CampaignResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kampanya (Campaign) REST uçları — apiController katmanı.
 */
@RestController
@RequestMapping("/api/v1/campaigns")
public class CampaignsController {

    private final CampaignService campaignService;

    public CampaignsController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse add(@Valid @RequestBody CreateCampaignRequest request) {
        return campaignService.add(request);
    }

    @GetMapping("/{id}")
    public CampaignResponse getById(@PathVariable Long id) {
        return campaignService.getById(id);
    }

    @GetMapping
    public PagedResponse<CampaignResponse> getAll(Pageable pageable) {
        return campaignService.getAll(pageable);
    }

    @PutMapping("/{id}")
    public CampaignResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateCampaignRequest request) {
        return campaignService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
