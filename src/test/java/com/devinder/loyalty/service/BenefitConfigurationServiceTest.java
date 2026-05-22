package com.devinder.loyalty.service;

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
import com.devinder.loyalty.service.impl.BenefitConfigurationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BenefitConfigurationServiceTest {

    @Mock
    private BenefitConfigurationRepository benefitConfigurationRepository;

    @Mock
    private MembershipBenefitRepository membershipBenefitRepository;

    @Mock
    private MembershipPlanRepository membershipPlanRepository;

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private BenefitConfigurationMapper benefitConfigurationMapper;

    @InjectMocks
    private BenefitConfigurationServiceImpl benefitConfigurationService;

    private BenefitConfiguration config;
    private MembershipBenefit benefit;
    private MembershipPlan plan;
    private MembershipTier tier;
    private CreateBenefitConfigurationRequest createRequest;
    private UpdateBenefitConfigurationRequest updateRequest;
    private BenefitConfigurationResponse configResponse;

    @BeforeEach
    void setUp() {
        benefit = MembershipBenefit.builder().name("Free Shipping").isActive(true).build();
        benefit.setId("benefit-123");

        plan = MembershipPlan.builder().name("Gold Plan").isActive(true).build();
        plan.setId("plan-123");

        tier = MembershipTier.builder().name("GOLD").priority(2).isActive(true).build();
        tier.setId("tier-123");

        config = BenefitConfiguration.builder()
                .membershipBenefit(benefit)
                .membershipPlan(plan)
                .membershipTier(tier)
                .configurationJson("{\"discount\": 10}")
                .isActive(true)
                .build();
        config.setId("config-123");

        createRequest = CreateBenefitConfigurationRequest.builder()
                .membershipBenefitId("benefit-123")
                .membershipPlanId("plan-123")
                .membershipTierId("tier-123")
                .configurationJson("{\"discount\": 10}")
                .build();

        updateRequest = UpdateBenefitConfigurationRequest.builder()
                .configurationJson("{\"discount\": 15}")
                .isActive(true)
                .build();

        configResponse = BenefitConfigurationResponse.builder()
                .id("config-123")
                .membershipBenefitId("benefit-123")
                .membershipPlanId("plan-123")
                .membershipTierId("tier-123")
                .configurationJson("{\"discount\": 10}")
                .isActive(true)
                .build();
    }

    @Test
    void createConfiguration_Success() {
        when(membershipBenefitRepository.findById("benefit-123")).thenReturn(Optional.of(benefit));
        when(membershipPlanRepository.findById("plan-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-123")).thenReturn(Optional.of(tier));
        when(benefitConfigurationRepository.existsActiveConfig("benefit-123", "plan-123", "tier-123")).thenReturn(false);
        when(benefitConfigurationRepository.save(any())).thenReturn(config);
        when(benefitConfigurationMapper.toResponse(any())).thenReturn(configResponse);

        BenefitConfigurationResponse result = benefitConfigurationService.createConfiguration(createRequest);

        assertNotNull(result);
        assertEquals(configResponse.getId(), result.getId());
        verify(benefitConfigurationRepository, times(1)).save(any());
    }

    @Test
    void createConfiguration_InvalidJson_ThrowsBadRequestException() {
        createRequest.setConfigurationJson("{invalid_json}");
        assertThrows(BadRequestException.class, () ->
                benefitConfigurationService.createConfiguration(createRequest));
        verify(benefitConfigurationRepository, never()).save(any());
    }

    @Test
    void createConfiguration_MissingPlanAndTier_ThrowsBadRequestException() {
        createRequest.setMembershipPlanId(null);
        createRequest.setMembershipTierId(null);

        assertThrows(BadRequestException.class, () ->
                benefitConfigurationService.createConfiguration(createRequest));
        verify(benefitConfigurationRepository, never()).save(any());
    }

    @Test
    void createConfiguration_DuplicateConfig_ThrowsConflictException() {
        when(membershipBenefitRepository.findById("benefit-123")).thenReturn(Optional.of(benefit));
        when(membershipPlanRepository.findById("plan-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-123")).thenReturn(Optional.of(tier));
        when(benefitConfigurationRepository.existsActiveConfig("benefit-123", "plan-123", "tier-123")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                benefitConfigurationService.createConfiguration(createRequest));
        verify(benefitConfigurationRepository, never()).save(any());
    }

    @Test
    void updateConfiguration_Success() {
        when(benefitConfigurationRepository.findById("config-123")).thenReturn(Optional.of(config));
        when(benefitConfigurationRepository.save(config)).thenReturn(config);
        when(benefitConfigurationMapper.toResponse(config)).thenReturn(configResponse);

        BenefitConfigurationResponse result = benefitConfigurationService.updateConfiguration("config-123", updateRequest);

        assertNotNull(result);
        verify(benefitConfigurationRepository, times(1)).save(config);
    }

    @Test
    void updateConfiguration_InvalidJson_ThrowsBadRequestException() {
        updateRequest.setConfigurationJson("{invalid}");
        assertThrows(BadRequestException.class, () ->
                benefitConfigurationService.updateConfiguration("config-123", updateRequest));
    }

    @Test
    void getConfigurationById_NotFound_ThrowsResourceNotFoundException() {
        when(benefitConfigurationRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                benefitConfigurationService.getConfigurationById("non-existent"));
    }

    @Test
    void deleteConfiguration_Success() {
        when(benefitConfigurationRepository.findById("config-123")).thenReturn(Optional.of(config));
        when(benefitConfigurationRepository.save(config)).thenReturn(config);

        assertDoesNotThrow(() -> benefitConfigurationService.deleteConfiguration("config-123"));

        assertFalse(config.getIsActive());
    }
}
