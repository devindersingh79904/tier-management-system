package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.request.CreateTierCriteriaRequest;
import com.devinder.loyalty.dto.request.UpdateTierCriteriaRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.TierCriteriaResponse;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.TierCriteria;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.TierCriteriaMapper;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.TierCriteriaRepository;
import com.devinder.loyalty.service.TierCriteriaService;
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
public class TierCriteriaServiceImpl implements TierCriteriaService {

    private final TierCriteriaRepository tierCriteriaRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final TierCriteriaMapper tierCriteriaMapper;

    @Override
    @Transactional
    public TierCriteriaResponse createCriteria(CreateTierCriteriaRequest request) {
        log.info("Creating tier criteria for tier ID: {}", request.getMembershipTierId());

        // 1. Validate JSON
        JsonValidationUtil.validateJsonFormat(request.getCriteriaJson());

        // 2. Validate tier existence
        MembershipTier tier = membershipTierRepository.findById(request.getMembershipTierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Membership tier with ID %s not found", request.getMembershipTierId())));

        // 3. Enforce only one active criteria config per tier
        if (tierCriteriaRepository.existsByMembershipTierIdAndIsActiveTrue(request.getMembershipTierId())) {
            throw new ConflictException(
                    String.format("An active tier criteria already exists for tier %s", tier.getName()));
        }

        TierCriteria criteria = TierCriteria.builder()
                .membershipTier(tier)
                .criteriaJson(request.getCriteriaJson())
                .isActive(true)
                .build();

        TierCriteria saved = tierCriteriaRepository.save(criteria);
        return tierCriteriaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TierCriteriaResponse updateCriteria(String id, UpdateTierCriteriaRequest request) {
        log.info("Updating tier criteria ID: {}", id);

        // 1. Validate JSON
        JsonValidationUtil.validateJsonFormat(request.getCriteriaJson());

        // 2. Find existing criteria
        TierCriteria criteria = tierCriteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Tier criteria with ID %s not found", id)));

        // 3. Enforce only one active criteria config per tier if changing state to active
        if (Boolean.FALSE.equals(criteria.getIsActive()) && Boolean.TRUE.equals(request.getIsActive())) {
            if (tierCriteriaRepository.existsByMembershipTierIdAndIsActiveTrue(criteria.getMembershipTier().getId())) {
                throw new ConflictException(
                        String.format("An active tier criteria already exists for tier %s", criteria.getMembershipTier().getName()));
            }
        }

        criteria.setCriteriaJson(request.getCriteriaJson());
        criteria.setIsActive(request.getIsActive());

        TierCriteria updated = tierCriteriaRepository.save(criteria);
        return tierCriteriaMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TierCriteriaResponse getCriteriaById(String id) {
        TierCriteria criteria = tierCriteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Tier criteria with ID %s not found", id)));
        return tierCriteriaMapper.toResponse(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TierCriteriaResponse> getAllCriteria(Pageable pageable) {
        Page<TierCriteria> page = tierCriteriaRepository.findAll(pageable);
        Page<TierCriteriaResponse> responsePage = page.map(tierCriteriaMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteCriteria(String id) {
        log.info("Soft deleting tier criteria ID: {}", id);
        TierCriteria criteria = tierCriteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Tier criteria with ID %s not found", id)));
        criteria.setIsActive(false);
        tierCriteriaRepository.save(criteria);
    }
}
