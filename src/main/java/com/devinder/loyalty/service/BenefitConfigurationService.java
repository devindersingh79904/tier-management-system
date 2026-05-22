package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.request.UpdateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.response.BenefitConfigurationResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface BenefitConfigurationService {
    BenefitConfigurationResponse createConfiguration(CreateBenefitConfigurationRequest request);
    BenefitConfigurationResponse updateConfiguration(String id, UpdateBenefitConfigurationRequest request);
    BenefitConfigurationResponse getConfigurationById(String id);
    PageResponse<BenefitConfigurationResponse> getAllConfigurations(Pageable pageable);
    void deleteConfiguration(String id);
}
