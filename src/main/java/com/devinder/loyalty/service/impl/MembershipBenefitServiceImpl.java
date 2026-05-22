package com.devinder.loyalty.service.impl;

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
import com.devinder.loyalty.service.MembershipBenefitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipBenefitServiceImpl implements MembershipBenefitService {

    private final MembershipBenefitRepository membershipBenefitRepository;
    private final BenefitConfigurationRepository benefitConfigurationRepository;
    private final MembershipBenefitMapper membershipBenefitMapper;

    @Override
    @Transactional
    public MembershipBenefitResponse createBenefit(CreateMembershipBenefitRequest request) {
        log.info("Creating membership benefit: {}", request.getName());

        if (membershipBenefitRepository.existsByName(request.getName())) {
            throw new ConflictException(
                    String.format(MessageConstants.BENEFIT_NAME_EXISTS, request.getName()));
        }

        MembershipBenefit benefit = membershipBenefitMapper.toEntity(request);
        MembershipBenefit saved = membershipBenefitRepository.save(benefit);
        return membershipBenefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MembershipBenefitResponse updateBenefit(String id, UpdateMembershipBenefitRequest request) {
        log.info("Updating membership benefit: {}", id);

        MembershipBenefit benefit = membershipBenefitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(MessageConstants.BENEFIT_ID_NOT_FOUND, id)));

        if (membershipBenefitRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException(
                    String.format(MessageConstants.BENEFIT_NAME_EXISTS, request.getName()));
        }

        // If trying to deactivate, check if active benefit configurations exist
        if (Boolean.TRUE.equals(benefit.getIsActive()) && Boolean.FALSE.equals(request.getIsActive())) {
            if (benefitConfigurationRepository.existsByMembershipBenefitIdAndIsActiveTrue(id)) {
                throw new ConflictException(MessageConstants.BENEFIT_HAS_ACTIVE_CONFIGURATIONS);
            }
        }

        membershipBenefitMapper.updateEntity(request, benefit);
        MembershipBenefit updated = membershipBenefitRepository.save(benefit);
        return membershipBenefitMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipBenefitResponse getBenefitById(String id) {
        MembershipBenefit benefit = membershipBenefitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(MessageConstants.BENEFIT_ID_NOT_FOUND, id)));
        return membershipBenefitMapper.toResponse(benefit);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipBenefitResponse> getActiveBenefits(Pageable pageable) {
        Page<MembershipBenefit> page = membershipBenefitRepository.findByIsActiveTrue(pageable);
        Page<MembershipBenefitResponse> responsePage = page.map(membershipBenefitMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipBenefitResponse> getAllBenefits(Pageable pageable) {
        Page<MembershipBenefit> page = membershipBenefitRepository.findAll(pageable);
        Page<MembershipBenefitResponse> responsePage = page.map(membershipBenefitMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteBenefit(String id) {
        log.info("Deleting membership benefit: {}", id);

        MembershipBenefit benefit = membershipBenefitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(MessageConstants.BENEFIT_ID_NOT_FOUND, id)));

        if (benefitConfigurationRepository.existsByMembershipBenefitIdAndIsActiveTrue(id)) {
            throw new ConflictException(MessageConstants.BENEFIT_HAS_ACTIVE_CONFIGURATIONS);
        }

        benefit.setIsActive(false);
        membershipBenefitRepository.save(benefit);
    }
}
