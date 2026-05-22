package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.MembershipPlanMapper;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.service.impl.MembershipPlanServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipPlanServiceTest {

    @Mock
    private MembershipPlanRepository membershipPlanRepository;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @Mock
    private MembershipPlanMapper membershipPlanMapper;

    @InjectMocks
    private MembershipPlanServiceImpl membershipPlanService;

    private MembershipPlan plan;
    private CreateMembershipPlanRequest createRequest;
    private UpdateMembershipPlanRequest updateRequest;
    private MembershipPlanResponse planResponse;

    @BeforeEach
    void setUp() {
        plan = MembershipPlan.builder()
                .name("Yearly Premium")
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(9900L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan.setId("plan-id-123");

        createRequest = CreateMembershipPlanRequest.builder()
                .name("Yearly Premium")
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(9900L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();

        updateRequest = UpdateMembershipPlanRequest.builder()
                .name("Yearly Premium")
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(9900L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();

        planResponse = MembershipPlanResponse.builder()
                .id("plan-id-123")
                .name("Yearly Premium")
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(9900L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
    }

    @Test
    void createPlan_Success() {
        when(membershipPlanRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(membershipPlanMapper.toEntity(createRequest)).thenReturn(plan);
        when(membershipPlanRepository.save(plan)).thenReturn(plan);
        when(membershipPlanMapper.toResponse(plan)).thenReturn(planResponse);

        MembershipPlanResponse result = membershipPlanService.createPlan(createRequest);

        assertNotNull(result);
        assertEquals(planResponse.getId(), result.getId());
        assertEquals(planResponse.getName(), result.getName());
        verify(membershipPlanRepository, times(1)).save(any());
    }

    @Test
    void createPlan_DuplicateName_ThrowsConflictException() {
        when(membershipPlanRepository.existsByName(createRequest.getName())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipPlanService.createPlan(createRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(membershipPlanRepository, never()).save(any());
    }

    @Test
    void updatePlan_Success() {
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipPlanRepository.existsByNameAndIdNot(updateRequest.getName(), "plan-id-123")).thenReturn(false);
        when(membershipPlanRepository.save(plan)).thenReturn(plan);
        when(membershipPlanMapper.toResponse(plan)).thenReturn(planResponse);

        MembershipPlanResponse result = membershipPlanService.updatePlan("plan-id-123", updateRequest);

        assertNotNull(result);
        verify(membershipPlanMapper, times(1)).updateEntityFromRequest(updateRequest, plan);
    }

    @Test
    void updatePlan_DeactivateWithActiveMemberships_ThrowsConflictException() {
        updateRequest.setIsActive(false);
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipPlanRepository.existsByNameAndIdNot(updateRequest.getName(), "plan-id-123")).thenReturn(false);
        when(userMembershipRepository.existsByMembershipPlanIdAndStatus("plan-id-123", MembershipStatus.ACTIVE)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipPlanService.updatePlan("plan-id-123", updateRequest));

        assertTrue(exception.getMessage().contains("Cannot deactivate plan because active memberships exist."));
        verify(membershipPlanRepository, never()).save(any());
    }

    @Test
    void deletePlan_Success() {
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(userMembershipRepository.existsByMembershipPlanIdAndStatus("plan-id-123", MembershipStatus.ACTIVE)).thenReturn(false);
        when(membershipPlanRepository.save(plan)).thenReturn(plan);

        assertDoesNotThrow(() -> membershipPlanService.deletePlan("plan-id-123"));

        assertFalse(plan.getIsActive());
        verify(membershipPlanRepository, times(1)).save(plan);
    }

    @Test
    void deletePlan_ActiveMembershipsExist_ThrowsConflictException() {
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(userMembershipRepository.existsByMembershipPlanIdAndStatus("plan-id-123", MembershipStatus.ACTIVE)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () ->
                membershipPlanService.deletePlan("plan-id-123"));

        assertTrue(exception.getMessage().contains("Cannot deactivate plan because active memberships exist."));
        verify(membershipPlanRepository, never()).save(any());
    }

    @Test
    void getPlanById_NotFound_ThrowsResourceNotFoundException() {
        when(membershipPlanRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                membershipPlanService.getPlanById("non-existent-id"));
    }

    @Test
    void getAllPlans_ReturnsPaginatedPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MembershipPlan> page = new PageImpl<>(Collections.singletonList(plan));
        when(membershipPlanRepository.findAll(pageable)).thenReturn(page);
        when(membershipPlanMapper.toResponse(plan)).thenReturn(planResponse);

        PageResponse<MembershipPlanResponse> result = membershipPlanService.getAllPlans(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getActivePlans_ReturnsPaginatedPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MembershipPlan> page = new PageImpl<>(Collections.singletonList(plan));
        when(membershipPlanRepository.findByIsActiveTrue(pageable)).thenReturn(page);
        when(membershipPlanMapper.toResponse(plan)).thenReturn(planResponse);

        PageResponse<MembershipPlanResponse> result = membershipPlanService.getActivePlans(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllPlansList_ReturnsList() {
        when(membershipPlanRepository.findAll()).thenReturn(Collections.singletonList(plan));
        when(membershipPlanMapper.toResponse(plan)).thenReturn(planResponse);

        List<MembershipPlanResponse> result = membershipPlanService.getAllPlans();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
