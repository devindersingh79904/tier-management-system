package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateTierCriteriaRequest;
import com.devinder.loyalty.dto.request.UpdateTierCriteriaRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.TierCriteriaResponse;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.TierCriteria;
import com.devinder.loyalty.exception.BadRequestException;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.TierCriteriaMapper;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.TierCriteriaRepository;
import com.devinder.loyalty.service.impl.TierCriteriaServiceImpl;
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
public class TierCriteriaServiceTest {

    @Mock
    private TierCriteriaRepository tierCriteriaRepository;

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private TierCriteriaMapper tierCriteriaMapper;

    @InjectMocks
    private TierCriteriaServiceImpl tierCriteriaService;

    private TierCriteria criteria;
    private MembershipTier tier;
    private CreateTierCriteriaRequest createRequest;
    private UpdateTierCriteriaRequest updateRequest;
    private TierCriteriaResponse criteriaResponse;

    @BeforeEach
    void setUp() {
        tier = MembershipTier.builder().name("GOLD").priority(2).isActive(true).build();
        tier.setId("tier-123");

        criteria = TierCriteria.builder()
                .membershipTier(tier)
                .criteriaJson("{\"operator\": \"AND\"}")
                .isActive(true)
                .build();
        criteria.setId("criteria-123");

        createRequest = CreateTierCriteriaRequest.builder()
                .membershipTierId("tier-123")
                .criteriaJson("{\"operator\": \"AND\"}")
                .build();

        updateRequest = UpdateTierCriteriaRequest.builder()
                .criteriaJson("{\"operator\": \"AND\", \"rules\": []}")
                .isActive(true)
                .build();

        criteriaResponse = TierCriteriaResponse.builder()
                .id("criteria-123")
                .membershipTierId("tier-123")
                .criteriaJson("{\"operator\": \"AND\"}")
                .isActive(true)
                .build();
    }

    @Test
    void createCriteria_Success() {
        when(membershipTierRepository.findById("tier-123")).thenReturn(Optional.of(tier));
        when(tierCriteriaRepository.existsByMembershipTierIdAndIsActiveTrue("tier-123")).thenReturn(false);
        when(tierCriteriaRepository.save(any())).thenReturn(criteria);
        when(tierCriteriaMapper.toResponse(any())).thenReturn(criteriaResponse);

        TierCriteriaResponse result = tierCriteriaService.createCriteria(createRequest);

        assertNotNull(result);
        assertEquals(criteriaResponse.getId(), result.getId());
        verify(tierCriteriaRepository, times(1)).save(any());
    }

    @Test
    void createCriteria_InvalidJson_ThrowsBadRequestException() {
        createRequest.setCriteriaJson("{invalid}");

        assertThrows(BadRequestException.class, () ->
                tierCriteriaService.createCriteria(createRequest));
        verify(tierCriteriaRepository, never()).save(any());
    }

    @Test
    void createCriteria_DuplicateActiveCriteria_ThrowsConflictException() {
        when(membershipTierRepository.findById("tier-123")).thenReturn(Optional.of(tier));
        when(tierCriteriaRepository.existsByMembershipTierIdAndIsActiveTrue("tier-123")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                tierCriteriaService.createCriteria(createRequest));
        verify(tierCriteriaRepository, never()).save(any());
    }

    @Test
    void updateCriteria_Success() {
        when(tierCriteriaRepository.findById("criteria-123")).thenReturn(Optional.of(criteria));
        when(tierCriteriaRepository.save(criteria)).thenReturn(criteria);
        when(tierCriteriaMapper.toResponse(criteria)).thenReturn(criteriaResponse);

        TierCriteriaResponse result = tierCriteriaService.updateCriteria("criteria-123", updateRequest);

        assertNotNull(result);
        verify(tierCriteriaRepository, times(1)).save(criteria);
    }

    @Test
    void updateCriteria_ActivateDuplicateCriteria_ThrowsConflictException() {
        criteria.setIsActive(false); // starts inactive
        when(tierCriteriaRepository.findById("criteria-123")).thenReturn(Optional.of(criteria));
        when(tierCriteriaRepository.existsByMembershipTierIdAndIsActiveTrue("tier-123")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                tierCriteriaService.updateCriteria("criteria-123", updateRequest));
        verify(tierCriteriaRepository, never()).save(any());
    }

    @Test
    void getCriteriaById_NotFound_ThrowsResourceNotFoundException() {
        when(tierCriteriaRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                tierCriteriaService.getCriteriaById("non-existent"));
    }

    @Test
    void deleteCriteria_Success() {
        when(tierCriteriaRepository.findById("criteria-123")).thenReturn(Optional.of(criteria));
        when(tierCriteriaRepository.save(criteria)).thenReturn(criteria);

        assertDoesNotThrow(() -> tierCriteriaService.deleteCriteria("criteria-123"));

        assertFalse(criteria.getIsActive());
    }
}
