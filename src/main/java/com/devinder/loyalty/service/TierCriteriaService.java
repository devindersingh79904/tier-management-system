package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateTierCriteriaRequest;
import com.devinder.loyalty.dto.request.UpdateTierCriteriaRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.TierCriteriaResponse;
import org.springframework.data.domain.Pageable;

public interface TierCriteriaService {
    TierCriteriaResponse createCriteria(CreateTierCriteriaRequest request);
    TierCriteriaResponse updateCriteria(String id, UpdateTierCriteriaRequest request);
    TierCriteriaResponse getCriteriaById(String id);
    PageResponse<TierCriteriaResponse> getAllCriteria(Pageable pageable);
    void deleteCriteria(String id);
}
