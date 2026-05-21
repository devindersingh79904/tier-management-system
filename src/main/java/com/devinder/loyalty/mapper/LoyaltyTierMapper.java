package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.request.LoyaltyTierRequest;
import com.devinder.loyalty.dto.response.LoyaltyTierResponse;
import com.devinder.loyalty.entity.LoyaltyTier;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface LoyaltyTierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    LoyaltyTier toEntity(LoyaltyTierRequest request);

    LoyaltyTierResponse toResponse(LoyaltyTier entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(LoyaltyTierRequest request, @MappingTarget LoyaltyTier entity);
}
