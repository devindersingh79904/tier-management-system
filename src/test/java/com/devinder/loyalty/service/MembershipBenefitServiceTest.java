package com.devinder.loyalty.service;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.request.CreateMembershipBenefitRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipBenefitRequest;
import com.devinder.loyalty.dto.response.MembershipBenefitResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.MembershipBenefitMapper;
import com.devinder.loyalty.repository.BenefitConfigurationRepository;
import com.devinder.loyalty.repository.MembershipBenefitRepository;
import com.devinder.loyalty.service.impl.MembershipBenefitServiceImpl;
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
public class MembershipBenefitServiceTest {

    @Mock
    private MembershipBenefitRepository membershipBenefitRepository;

    @Mock
    private BenefitConfigurationRepository benefitConfigurationRepository;

    @Mock
    private MembershipBenefitMapper membershipBenefitMapper;

    @InjectMocks
    private MembershipBenefitServiceImpl membershipBenefitService;

    private MembershipBenefit benefit;
    private CreateMembershipBenefitRequest createRequest;
    private UpdateMembershipBenefitRequest updateRequest;
    private MembershipBenefitResponse benefitResponse;

    @BeforeEach
    void setUp() {
        benefit = MembershipBenefit.builder()
                .name("Free Shipping")
                .description("Get free shipping on all orders")
                .isActive(true)
                .build();
        benefit.setId("benefit-id-123");

        createRequest = CreateMembershipBenefitRequest.builder()
                .name("Free Shipping")
                .description("Get free shipping on all orders")
                .build();

        updateRequest = UpdateMembershipBenefitRequest.builder()
                .name("Free Shipping")
                .description("Get free shipping on all orders updated")
                .isActive(true)
                .build();

        benefitResponse = MembershipBenefitResponse.builder()
                .id("benefit-id-123")
                .name("Free Shipping")
                .description("Get free shipping on all orders")
                .isActive(true)
                .build();
    }

    @Test
    void createBenefit_Success() {
        when(membershipBenefitRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(membershipBenefitMapper.toEntity(createRequest)).thenReturn(benefit);
        when(membershipBenefitRepository.save(benefit)).thenReturn(benefit);
        when(membershipBenefitMapper.toResponse(benefit)).thenReturn(benefitResponse);

        MembershipBenefitResponse result = membershipBenefitService.createBenefit(createRequest);

        assertNotNull(result);
        assertEquals(benefitResponse.getId(), result.getId());
        assertEquals(benefitResponse.getName(), result.getName());
        verify(membershipBenefitRepository, times(1)).save(any());
    }

    @Test
    void createBenefit_DuplicateName_ThrowsConflictException() {
        when(membershipBenefitRepository.existsByName(createRequest.getName())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipBenefitService.createBenefit(createRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(membershipBenefitRepository, never()).save(any());
    }

    @Test
    void updateBenefit_Success() {
        when(membershipBenefitRepository.findById("benefit-id-123")).thenReturn(Optional.of(benefit));
        when(membershipBenefitRepository.existsByNameAndIdNot(updateRequest.getName(), "benefit-id-123")).thenReturn(false);
        when(membershipBenefitRepository.save(benefit)).thenReturn(benefit);
        when(membershipBenefitMapper.toResponse(benefit)).thenReturn(benefitResponse);

        MembershipBenefitResponse result = membershipBenefitService.updateBenefit("benefit-id-123", updateRequest);

        assertNotNull(result);
        verify(membershipBenefitMapper, times(1)).updateEntity(updateRequest, benefit);
    }

    @Test
    void updateBenefit_DeactivateWithActiveConfigs_ThrowsConflictException() {
        updateRequest.setIsActive(false);
        when(membershipBenefitRepository.findById("benefit-id-123")).thenReturn(Optional.of(benefit));
        when(membershipBenefitRepository.existsByNameAndIdNot(updateRequest.getName(), "benefit-id-123")).thenReturn(false);
        when(benefitConfigurationRepository.existsByMembershipBenefitIdAndIsActiveTrue("benefit-id-123")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipBenefitService.updateBenefit("benefit-id-123", updateRequest));

        assertEquals(MessageConstants.BENEFIT_HAS_ACTIVE_CONFIGURATIONS, exception.getMessage());
        verify(membershipBenefitRepository, never()).save(any());
    }

    @Test
    void deleteBenefit_Success() {
        when(membershipBenefitRepository.findById("benefit-id-123")).thenReturn(Optional.of(benefit));
        when(benefitConfigurationRepository.existsByMembershipBenefitIdAndIsActiveTrue("benefit-id-123")).thenReturn(false);
        when(membershipBenefitRepository.save(benefit)).thenReturn(benefit);

        assertDoesNotThrow(() -> membershipBenefitService.deleteBenefit("benefit-id-123"));

        assertFalse(benefit.getIsActive());
        verify(membershipBenefitRepository, times(1)).save(benefit);
    }

    @Test
    void deleteBenefit_ActiveConfigsExist_ThrowsConflictException() {
        when(membershipBenefitRepository.findById("benefit-id-123")).thenReturn(Optional.of(benefit));
        when(benefitConfigurationRepository.existsByMembershipBenefitIdAndIsActiveTrue("benefit-id-123")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipBenefitService.deleteBenefit("benefit-id-123"));

        assertEquals(MessageConstants.BENEFIT_HAS_ACTIVE_CONFIGURATIONS, exception.getMessage());
        verify(membershipBenefitRepository, never()).save(any());
    }

    @Test
    void getBenefitById_NotFound_ThrowsResourceNotFoundException() {
        when(membershipBenefitRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                membershipBenefitService.getBenefitById("non-existent-id"));
    }

    @Test
    void getAllBenefits_ReturnsPaginatedPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MembershipBenefit> page = new PageImpl<>(Collections.singletonList(benefit));
        when(membershipBenefitRepository.findAll(pageable)).thenReturn(page);
        when(membershipBenefitMapper.toResponse(benefit)).thenReturn(benefitResponse);

        PageResponse<MembershipBenefitResponse> result = membershipBenefitService.getAllBenefits(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getActiveBenefits_ReturnsPaginatedPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MembershipBenefit> page = new PageImpl<>(Collections.singletonList(benefit));
        when(membershipBenefitRepository.findByIsActiveTrue(pageable)).thenReturn(page);
        when(membershipBenefitMapper.toResponse(benefit)).thenReturn(benefitResponse);

        PageResponse<MembershipBenefitResponse> result = membershipBenefitService.getActiveBenefits(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
