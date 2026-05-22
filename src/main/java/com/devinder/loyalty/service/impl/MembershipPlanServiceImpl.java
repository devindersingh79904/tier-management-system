package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.MembershipPlanMapper;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final MembershipPlanMapper membershipPlanMapper;

    @Override
    @Transactional
    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        log.info("Creating membership plan with name: {}", request.getName());

        if (membershipPlanRepository.existsByName(request.getName())) {
            throw new ConflictException("Membership plan with name '" + request.getName() + "' already exists");
        }

        MembershipPlan plan = membershipPlanMapper.toEntity(request);
        MembershipPlan savedPlan = membershipPlanRepository.save(plan);

        return membershipPlanMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional
    public MembershipPlanResponse updatePlan(String id, UpdateMembershipPlanRequest request) {
        log.info("Updating membership plan with id: {}", id);
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));

        if (membershipPlanRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Membership plan with name '" + request.getName() + "' already exists");
        }

        // Enforce deletion rule: if setting isActive = false, check active memberships
        if (Boolean.FALSE.equals(request.getIsActive()) && Boolean.TRUE.equals(plan.getIsActive())) {
            if (userMembershipRepository.existsByMembershipPlanIdAndStatus(id, MembershipStatus.ACTIVE)) {
                throw new ConflictException("Cannot deactivate plan because active memberships exist.");
            }
        }

        membershipPlanMapper.updateEntityFromRequest(request, plan);
        MembershipPlan updatedPlan = membershipPlanRepository.save(plan);
        return membershipPlanMapper.toResponse(updatedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse getPlanById(String id) {
        log.info("Fetching membership plan with id: {}", id);
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));
        return membershipPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional
    public void deletePlan(String id) {
        log.info("Soft deleting membership plan with id: {}", id);
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));

        if (userMembershipRepository.existsByMembershipPlanIdAndStatus(id, MembershipStatus.ACTIVE)) {
            throw new ConflictException("Cannot deactivate plan because active memberships exist.");
        }

        plan.setIsActive(false);
        membershipPlanRepository.save(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipPlanResponse> getActivePlans(Pageable pageable) {
        log.info("Fetching active membership plans paginated");
        return PageResponse.from(membershipPlanRepository.findByIsActiveTrue(pageable)
                .map(membershipPlanMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipPlanResponse> getAllPlans(Pageable pageable) {
        log.info("Fetching all membership plans paginated");
        return PageResponse.from(membershipPlanRepository.findAll(pageable)
                .map(membershipPlanMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getAllPlans() {
        log.info("Fetching all membership plans");
        return membershipPlanRepository.findAll().stream()
                .map(membershipPlanMapper::toResponse)
                .collect(Collectors.toList());
    }
}
