package com.devinder.loyalty.service;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.request.CreateMembershipTierRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipTierRequest;
import com.devinder.loyalty.dto.response.MembershipTierResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.MembershipTierMapper;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.service.impl.MembershipTierServiceImpl;
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
public class MembershipTierServiceTest {

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @Mock
    private MembershipTierMapper membershipTierMapper;

    @InjectMocks
    private MembershipTierServiceImpl membershipTierService;

    private MembershipTier tier;
    private CreateMembershipTierRequest createRequest;
    private UpdateMembershipTierRequest updateRequest;
    private MembershipTierResponse tierResponse;

    @BeforeEach
    void setUp() {
        tier = MembershipTier.builder()
                .name("GOLD")
                .priority(2)
                .description("Gold tier")
                .isActive(true)
                .build();
        tier.setId("tier-id-123");

        createRequest = CreateMembershipTierRequest.builder()
                .name("GOLD")
                .priority(2)
                .description("Gold tier")
                .isActive(true)
                .build();

        updateRequest = UpdateMembershipTierRequest.builder()
                .name("GOLD")
                .priority(2)
                .description("Gold tier updated")
                .isActive(true)
                .build();

        tierResponse = MembershipTierResponse.builder()
                .id("tier-id-123")
                .name("GOLD")
                .priority(2)
                .description("Gold tier")
                .isActive(true)
                .build();
    }

    @Test
    void createTier_Success() {
        when(membershipTierRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(membershipTierRepository.existsByPriority(createRequest.getPriority())).thenReturn(false);
        when(membershipTierMapper.toEntity(createRequest)).thenReturn(tier);
        when(membershipTierRepository.save(tier)).thenReturn(tier);
        when(membershipTierMapper.toResponse(tier)).thenReturn(tierResponse);

        MembershipTierResponse result = membershipTierService.createTier(createRequest);

        assertNotNull(result);
        assertEquals(tierResponse.getId(), result.getId());
        assertEquals(tierResponse.getName(), result.getName());
        verify(membershipTierRepository, times(1)).save(any());
    }

    @Test
    void createTier_DuplicateName_ThrowsConflictException() {
        when(membershipTierRepository.existsByName(createRequest.getName())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipTierService.createTier(createRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(membershipTierRepository, never()).save(any());
    }

    @Test
    void createTier_DuplicatePriority_ThrowsConflictException() {
        when(membershipTierRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(membershipTierRepository.existsByPriority(createRequest.getPriority())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipTierService.createTier(createRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(membershipTierRepository, never()).save(any());
    }

    @Test
    void updateTier_Success() {
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(membershipTierRepository.existsByNameAndIdNot(updateRequest.getName(), "tier-id-123")).thenReturn(false);
        when(membershipTierRepository.existsByPriorityAndIdNot(updateRequest.getPriority(), "tier-id-123")).thenReturn(false);
        when(membershipTierRepository.save(tier)).thenReturn(tier);
        when(membershipTierMapper.toResponse(tier)).thenReturn(tierResponse);

        MembershipTierResponse result = membershipTierService.updateTier("tier-id-123", updateRequest);

        assertNotNull(result);
        verify(membershipTierMapper, times(1)).updateEntity(updateRequest, tier);
    }

    @Test
    void updateTier_DeactivateWithActiveMemberships_ThrowsConflictException() {
        updateRequest.setIsActive(false);
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(membershipTierRepository.existsByNameAndIdNot(updateRequest.getName(), "tier-id-123")).thenReturn(false);
        when(membershipTierRepository.existsByPriorityAndIdNot(updateRequest.getPriority(), "tier-id-123")).thenReturn(false);
        when(userMembershipRepository.existsByMembershipTierIdAndStatus("tier-id-123", MembershipStatus.ACTIVE)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipTierService.updateTier("tier-id-123", updateRequest));

        assertEquals(MessageConstants.TIER_HAS_ACTIVE_MEMBERSHIPS, exception.getMessage());
        verify(membershipTierRepository, never()).save(any());
    }

    @Test
    void deleteTier_Success() {
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(userMembershipRepository.existsByMembershipTierIdAndStatus("tier-id-123", MembershipStatus.ACTIVE)).thenReturn(false);
        when(membershipTierRepository.save(tier)).thenReturn(tier);

        assertDoesNotThrow(() -> membershipTierService.deleteTier("tier-id-123"));

        assertFalse(tier.getIsActive());
        verify(membershipTierRepository, times(1)).save(tier);
    }

    @Test
    void deleteTier_ActiveMembershipsExist_ThrowsConflictException() {
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(userMembershipRepository.existsByMembershipTierIdAndStatus("tier-id-123", MembershipStatus.ACTIVE)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipTierService.deleteTier("tier-id-123"));

        assertEquals(MessageConstants.TIER_HAS_ACTIVE_MEMBERSHIPS, exception.getMessage());
        verify(membershipTierRepository, never()).save(any());
    }

    @Test
    void getTierById_NotFound_ThrowsResourceNotFoundException() {
        when(membershipTierRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                membershipTierService.getTierById("non-existent-id"));
    }

    @Test
    void getAllTiers_ReturnsPaginatedPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MembershipTier> page = new PageImpl<>(Collections.singletonList(tier));
        when(membershipTierRepository.findAll(pageable)).thenReturn(page);
        when(membershipTierMapper.toResponse(tier)).thenReturn(tierResponse);

        PageResponse<MembershipTierResponse> result = membershipTierService.getAllTiers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalElements());
    }
}
