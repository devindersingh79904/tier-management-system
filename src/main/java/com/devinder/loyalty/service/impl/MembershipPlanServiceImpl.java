package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.mapper.MembershipPlanMapper;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipPlanMapper membershipPlanMapper;

    @Override
    @Transactional
    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        log.info("Creating membership plan with name: {}", request.getName());

        if (membershipPlanRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException("Membership plan with name '" + request.getName() + "' already exists");
        }

        MembershipPlan plan = membershipPlanMapper.toEntity(request);
        MembershipPlan savedPlan = membershipPlanRepository.save(plan);

        return membershipPlanMapper.toResponse(savedPlan);
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
