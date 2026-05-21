package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.request.LoyaltyTierRequest;
import com.devinder.loyalty.dto.response.LoyaltyTierResponse;
import com.devinder.loyalty.entity.LoyaltyTier;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.LoyaltyTierMapper;
import com.devinder.loyalty.repository.LoyaltyTierRepository;
import com.devinder.loyalty.service.LoyaltyTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyTierServiceImpl implements LoyaltyTierService {

    private final LoyaltyTierRepository repository;
    private final LoyaltyTierMapper mapper;

    @Override
    @Transactional
    public LoyaltyTierResponse createTier(LoyaltyTierRequest request) {
        log.info("Creating new loyalty tier with name: {}", request.getName());
        
        repository.findByName(request.getName()).ifPresent(tier -> {
            throw new ConflictException(String.format(MessageConstants.TIER_NAME_EXISTS, request.getName()));
        });

        LoyaltyTier tier = mapper.toEntity(request);
        LoyaltyTier savedTier = repository.save(tier);
        log.info("Successfully created loyalty tier with ID: {}", savedTier.getId());
        return mapper.toResponse(savedTier);
    }

    @Override
    @Transactional(readOnly = true)
    public LoyaltyTierResponse getTierById(Long id) {
        log.info("Fetching loyalty tier with ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MessageConstants.TIER_ID_NOT_FOUND, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public LoyaltyTierResponse getTierByName(String name) {
        log.info("Fetching loyalty tier with name: {}", name);
        return repository.findByName(name)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MessageConstants.TIER_NAME_NOT_FOUND, name)));
    }

    @Override
    @Transactional
    public LoyaltyTierResponse updateTier(Long id, LoyaltyTierRequest request) {
        log.info("Updating loyalty tier with ID: {}", id);
        
        LoyaltyTier existingTier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MessageConstants.TIER_ID_NOT_FOUND, id)));

        // Check if name is changed and if new name is already taken
        if (!existingTier.getName().equals(request.getName())) {
            repository.findByName(request.getName()).ifPresent(tier -> {
                throw new ConflictException(String.format(MessageConstants.TIER_NAME_EXISTS, request.getName()));
            });
        }

        mapper.updateEntity(request, existingTier);
        LoyaltyTier updatedTier = repository.save(existingTier);
        log.info("Successfully updated loyalty tier with ID: {}", updatedTier.getId());
        return mapper.toResponse(updatedTier);
    }

    @Override
    @Transactional
    public void deleteTier(Long id) {
        log.info("Deleting loyalty tier with ID: {}", id);
        LoyaltyTier existingTier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MessageConstants.TIER_ID_NOT_FOUND, id)));
        repository.delete(existingTier);
        log.info("Successfully deleted loyalty tier with ID: {}", id);
    }
}
