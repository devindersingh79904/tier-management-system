package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.request.CreateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.request.UpdateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.response.BenefitConfigurationResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.entity.BenefitConfiguration;
import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.exception.BadRequestException;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.BenefitConfigurationMapper;
import com.devinder.loyalty.repository.BenefitConfigurationRepository;
import com.devinder.loyalty.repository.MembershipBenefitRepository;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.service.BenefitConfigurationService;
import com.devinder.loyalty.util.JsonValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitConfigurationServiceImpl implements BenefitConfigurationService {

    private final BenefitConfigurationRepository benefitConfigurationRepository;
    private final MembershipBenefitRepository membershipBenefitRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final BenefitConfigurationMapper benefitConfigurationMapper;

    @Override
    @Transactional
    public BenefitConfigurationResponse createConfiguration(CreateBenefitConfigurationRequest request) {
        log.info("Creating benefit configuration for benefit ID: {}", request.getMembershipBenefitId());

        // 1. Validate JSON
        JsonValidationUtil.validateJsonFormat(request.getConfigurationJson());

        // 2. Validate at least plan or tier is specified
        String planId = request.getMembershipPlanId();
        String tierId = request.getMembershipTierId();
        if ((planId == null || planId.trim().isEmpty()) && (tierId == null || tierId.trim().isEmpty())) {
            throw new BadRequestException("Either membershipPlanId or membershipTierId must be specified");
        }

        // 3. Find and validate benefit
        MembershipBenefit benefit = membershipBenefitRepository.findById(request.getMembershipBenefitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Membership benefit with ID %s not found", request.getMembershipBenefitId())));

        // 4. Find plan
        MembershipPlan plan = null;
        if (planId != null && !planId.trim().isEmpty()) {
            plan = membershipPlanRepository.findById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Membership plan with ID %s not found", planId)));
        }

        // 5. Find tier
        MembershipTier tier = null;
        if (tierId != null && !tierId.trim().isEmpty()) {
            tier = membershipTierRepository.findById(tierId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Membership tier with ID %s not found", tierId)));
        }

        // 6. Check for duplicate active configurations
        String finalPlanId = (plan != null) ? plan.getId() : null;
        String finalTierId = (tier != null) ? tier.getId() : null;
        if (benefitConfigurationRepository.existsActiveConfig(benefit.getId(), finalPlanId, finalTierId)) {
            throw new ConflictException("An active benefit configuration already exists for this benefit, plan, and tier combination");
        }

        BenefitConfiguration config = BenefitConfiguration.builder()
                .membershipBenefit(benefit)
                .membershipPlan(plan)
                .membershipTier(tier)
                .configurationJson(request.getConfigurationJson())
                .isActive(true)
                .build();

        BenefitConfiguration saved = benefitConfigurationRepository.save(config);
        return benefitConfigurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BenefitConfigurationResponse updateConfiguration(String id, UpdateBenefitConfigurationRequest request) {
        log.info("Updating benefit configuration: {}", id);

        // 1. Validate JSON
        JsonValidationUtil.validateJsonFormat(request.getConfigurationJson());

        // 2. Find configuration
        BenefitConfiguration config = benefitConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Benefit configuration with ID %s not found", id)));

        config.setConfigurationJson(request.getConfigurationJson());
        config.setIsActive(request.getIsActive());

        BenefitConfiguration updated = benefitConfigurationRepository.save(config);
        return benefitConfigurationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BenefitConfigurationResponse getConfigurationById(String id) {
        BenefitConfiguration config = benefitConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Benefit configuration with ID %s not found", id)));
        return benefitConfigurationMapper.toResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BenefitConfigurationResponse> getAllConfigurations(Pageable pageable) {
        Page<BenefitConfiguration> page = benefitConfigurationRepository.findAll(pageable);
        Page<BenefitConfigurationResponse> responsePage = page.map(benefitConfigurationMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteConfiguration(String id) {
        log.info("Soft deleting benefit configuration: {}", id);
        BenefitConfiguration config = benefitConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Benefit configuration with ID %s not found", id)));
        config.setIsActive(false);
        benefitConfigurationRepository.save(config);
    }
}
