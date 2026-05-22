package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CancelMembershipRequest;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.request.UpgradeMembershipRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.UserMembershipMapper;
import com.devinder.loyalty.repository.MembershipEventRepository;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.impl.UserMembershipServiceImpl;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserMembershipServiceTest {

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipPlanRepository membershipPlanRepository;

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private MembershipEventRepository membershipEventRepository;

    @Mock
    private UserMembershipMapper userMembershipMapper;

    @InjectMocks
    private UserMembershipServiceImpl userMembershipService;

    private User user;
    private MembershipPlan plan;
    private MembershipTier tier;
    private MembershipTier goldTier;
    private UserMembership membership;
    private UserMembershipResponse membershipResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("John Doe")
                .mobileNumber("1234567890")
                .build();
        user.setId("user-id-123");

        plan = MembershipPlan.builder()
                .name("Yearly Plan")
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(9900L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan.setId("plan-id-123");

        tier = MembershipTier.builder()
                .name("SILVER")
                .priority(1)
                .isActive(true)
                .build();
        tier.setId("tier-id-123");

        goldTier = MembershipTier.builder()
                .name("GOLD")
                .priority(2)
                .isActive(true)
                .build();
        goldTier.setId("tier-id-456");

        membership = UserMembership.builder()
                .user(user)
                .membershipPlan(plan)
                .membershipTier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600 * 24 * 365))
                .purchasedPrice(9900L)
                .discountAmount(0L)
                .finalPrice(9900L)
                .autoRenew(true)
                .build();
        membership.setId("membership-id-123");

        membershipResponse = UserMembershipResponse.builder()
                .id("membership-id-123")
                .userId("user-id-123")
                .userName("John Doe")
                .userMobileNumber("1234567890")
                .membershipPlanId("plan-id-123")
                .membershipPlanName("Yearly Plan")
                .membershipTierId("tier-id-123")
                .membershipTierName("SILVER")
                .status(MembershipStatus.ACTIVE)
                .purchasedPrice(9900L)
                .discountAmount(0L)
                .finalPrice(9900L)
                .autoRenew(true)
                .build();
    }

    @Test
    void createMembership_Success_WithUserId() {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .userId("user-id-123")
                .membershipPlanId("plan-id-123")
                .membershipTierId("tier-id-123")
                .autoRenew(true)
                .build();

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(userMembershipRepository.existsByUserIdAndStatus("user-id-123", MembershipStatus.ACTIVE)).thenReturn(false);
        when(userMembershipRepository.save(any(UserMembership.class))).thenReturn(membership);
        when(userMembershipMapper.toResponse(any(UserMembership.class))).thenReturn(membershipResponse);

        UserMembershipResponse result = userMembershipService.createMembership(request, "1234567890");

        assertNotNull(result);
        assertEquals(membershipResponse.getId(), result.getId());
        verify(userMembershipRepository, times(1)).save(any());
        verify(membershipEventRepository, times(1)).save(any(MembershipEvent.class));
    }

    @Test
    void createMembership_Success_WithDefaultUsername() {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .membershipPlanId("plan-id-123")
                .membershipTierId("tier-id-123")
                .autoRenew(true)
                .build();

        when(userRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(userMembershipRepository.existsByUserIdAndStatus("user-id-123", MembershipStatus.ACTIVE)).thenReturn(false);
        when(userMembershipRepository.save(any(UserMembership.class))).thenReturn(membership);
        when(userMembershipMapper.toResponse(any(UserMembership.class))).thenReturn(membershipResponse);

        UserMembershipResponse result = userMembershipService.createMembership(request, "1234567890");

        assertNotNull(result);
        verify(userMembershipRepository, times(1)).save(any());
    }

    @Test
    void createMembership_InactivePlan_ThrowsConflictException() {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .userId("user-id-123")
                .membershipPlanId("plan-id-123")
                .membershipTierId("tier-id-123")
                .autoRenew(true)
                .build();

        plan.setIsActive(false);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));

        assertThrows(ConflictException.class, () ->
                userMembershipService.createMembership(request, "1234567890"));
    }

    @Test
    void createMembership_InactiveTier_ThrowsConflictException() {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .userId("user-id-123")
                .membershipPlanId("plan-id-123")
                .membershipTierId("tier-id-123")
                .autoRenew(true)
                .build();

        tier.setIsActive(false);

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));

        assertThrows(ConflictException.class, () ->
                userMembershipService.createMembership(request, "1234567890"));
    }

    @Test
    void createMembership_AlreadyHasActiveSubscription_ThrowsConflictException() {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .userId("user-id-123")
                .membershipPlanId("plan-id-123")
                .membershipTierId("tier-id-123")
                .autoRenew(true)
                .build();

        when(userRepository.findById("user-id-123")).thenReturn(Optional.of(user));
        when(membershipPlanRepository.findById("plan-id-123")).thenReturn(Optional.of(plan));
        when(membershipTierRepository.findById("tier-id-123")).thenReturn(Optional.of(tier));
        when(userMembershipRepository.existsByUserIdAndStatus("user-id-123", MembershipStatus.ACTIVE)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                userMembershipService.createMembership(request, "1234567890"));
    }

    @Test
    void upgradeMembership_Success() {
        UpgradeMembershipRequest request = UpgradeMembershipRequest.builder()
                .membershipTierId("tier-id-456")
                .build();

        when(userMembershipRepository.findById("membership-id-123")).thenReturn(Optional.of(membership));
        when(membershipTierRepository.findById("tier-id-456")).thenReturn(Optional.of(goldTier));
        when(userMembershipRepository.save(membership)).thenReturn(membership);
        when(userMembershipMapper.toResponse(membership)).thenReturn(membershipResponse);

        UserMembershipResponse result = userMembershipService.upgradeMembership("membership-id-123", request);

        assertNotNull(result);
        verify(userMembershipRepository, times(1)).save(membership);
        verify(membershipEventRepository, times(1)).save(any(MembershipEvent.class));
    }

    @Test
    void upgradeMembership_LowerPriorityTier_ThrowsConflictException() {
        UpgradeMembershipRequest request = UpgradeMembershipRequest.builder()
                .membershipTierId("tier-id-456")
                .build();

        goldTier.setPriority(1); // Same or lower priority

        when(userMembershipRepository.findById("membership-id-123")).thenReturn(Optional.of(membership));
        when(membershipTierRepository.findById("tier-id-456")).thenReturn(Optional.of(goldTier));

        assertThrows(ConflictException.class, () ->
                userMembershipService.upgradeMembership("membership-id-123", request));
    }

    @Test
    void cancelMembership_Success() {
        CancelMembershipRequest request = CancelMembershipRequest.builder()
                .reason("No longer needed")
                .build();

        when(userMembershipRepository.findById("membership-id-123")).thenReturn(Optional.of(membership));
        when(userMembershipRepository.save(membership)).thenReturn(membership);
        when(userMembershipMapper.toResponse(membership)).thenReturn(membershipResponse);

        UserMembershipResponse result = userMembershipService.cancelMembership("membership-id-123", request);

        assertNotNull(result);
        assertEquals(MembershipStatus.CANCELLED, membership.getStatus());
        verify(userMembershipRepository, times(1)).save(membership);
    }

    @Test
    void expireMemberships_Success() {
        when(userMembershipRepository.findByEndDateBeforeAndStatus(any(Instant.class), eq(MembershipStatus.ACTIVE)))
                .thenReturn(Collections.singletonList(membership));
        when(userMembershipRepository.save(membership)).thenReturn(membership);

        userMembershipService.expireMemberships();

        assertEquals(MembershipStatus.EXPIRED, membership.getStatus());
        verify(userMembershipRepository, times(1)).save(membership);
    }

    @Test
    void getMyMembershipsHistory_Success() {
        when(userRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(user));
        when(userMembershipRepository.findByUserId("user-id-123")).thenReturn(Collections.singletonList(membership));
        when(userMembershipMapper.toResponse(membership)).thenReturn(membershipResponse);

        List<UserMembershipResponse> result = userMembershipService.getMyMembershipsHistory("1234567890");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyActiveMembership_Success() {
        when(userRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(user));
        when(userMembershipRepository.findByUserIdAndStatus("user-id-123", MembershipStatus.ACTIVE))
                .thenReturn(Collections.singletonList(membership));
        when(userMembershipMapper.toResponse(membership)).thenReturn(membershipResponse);

        UserMembershipResponse result = userMembershipService.getMyActiveMembership("1234567890");

        assertNotNull(result);
        assertEquals(membershipResponse.getId(), result.getId());
    }

    @Test
    void getMyActiveMembership_None_ThrowsResourceNotFoundException() {
        when(userRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(user));
        when(userMembershipRepository.findByUserIdAndStatus("user-id-123", MembershipStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () ->
                userMembershipService.getMyActiveMembership("1234567890"));
    }

    @Test
    void getMembershipById_NotFound_ThrowsResourceNotFoundException() {
        when(userMembershipRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userMembershipService.getMembershipById("non-existent-id"));
    }

    @Test
    void getAllMemberships_ReturnsPaginatedPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserMembership> page = new PageImpl<>(Collections.singletonList(membership));
        when(userMembershipRepository.findAll(pageable)).thenReturn(page);
        when(userMembershipMapper.toResponse(membership)).thenReturn(membershipResponse);

        PageResponse<UserMembershipResponse> result = userMembershipService.getAllMemberships(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllMembershipsList_ReturnsList() {
        when(userMembershipRepository.findAll()).thenReturn(Collections.singletonList(membership));
        when(userMembershipMapper.toResponse(membership)).thenReturn(membershipResponse);

        List<UserMembershipResponse> result = userMembershipService.getAllMemberships();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
