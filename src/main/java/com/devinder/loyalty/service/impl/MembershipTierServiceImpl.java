package com.devinder.loyalty.service.impl;

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
import com.devinder.loyalty.service.MembershipTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipTierServiceImpl implements MembershipTierService {

    private final MembershipTierRepository membershipTierRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final MembershipTierMapper membershipTierMapper;

    @Override
    @Transactional
    public MembershipTierResponse createTier(CreateMembershipTierRequest request) {
        log.info("Creating membership tier: {}", request.getName());

        if (membershipTierRepository.existsByName(request.getName())) {
            throw new ConflictException(
                    String.format(MessageConstants.TIER_NAME_EXISTS, request.getName()));
        }

        if (membershipTierRepository.existsByPriority(request.getPriority())) {
            throw new ConflictException(
                    String.format(MessageConstants.TIER_PRIORITY_EXISTS, request.getPriority()));
        }

        MembershipTier tier = membershipTierMapper.toEntity(request);
        MembershipTier saved = membershipTierRepository.save(tier);
        return membershipTierMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MembershipTierResponse updateTier(String id, UpdateMembershipTierRequest request) {
        log.info("Updating membership tier: {}", id);

        MembershipTier tier = membershipTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(MessageConstants.TIER_ID_NOT_FOUND, id)));

        if (membershipTierRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException(
                    String.format(MessageConstants.TIER_NAME_EXISTS, request.getName()));
        }

        if (membershipTierRepository.existsByPriorityAndIdNot(request.getPriority(), id)) {
            throw new ConflictException(
                    String.format(MessageConstants.TIER_PRIORITY_EXISTS, request.getPriority()));
        }

        // If tier was active and request is trying to deactivate it, verify no active memberships exist
        if (Boolean.TRUE.equals(tier.getIsActive()) && Boolean.FALSE.equals(request.getIsActive())) {
            if (userMembershipRepository.existsByMembershipTierIdAndStatus(
                    id, MembershipStatus.ACTIVE)) {
                throw new ConflictException(MessageConstants.TIER_HAS_ACTIVE_MEMBERSHIPS);
            }
        }

        membershipTierMapper.updateEntity(request, tier);
        MembershipTier updated = membershipTierRepository.save(tier);
        return membershipTierMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipTierResponse getTierById(String id) {
        MembershipTier tier = membershipTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(MessageConstants.TIER_ID_NOT_FOUND, id)));
        return membershipTierMapper.toResponse(tier);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipTierResponse> getAllTiers(Pageable pageable) {
        Page<MembershipTier> page = membershipTierRepository.findAll(pageable);
        Page<MembershipTierResponse> responsePage = page.map(membershipTierMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteTier(String id) {
        log.info("Deleting membership tier: {}", id);

        MembershipTier tier = membershipTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(MessageConstants.TIER_ID_NOT_FOUND, id)));

        // Verify no active memberships exist before deactivating/deleting
        if (userMembershipRepository.existsByMembershipTierIdAndStatus(
                id, MembershipStatus.ACTIVE)) {
            throw new ConflictException(MessageConstants.TIER_HAS_ACTIVE_MEMBERSHIPS);
        }

        tier.setIsActive(false);
        membershipTierRepository.save(tier);
    }
}
