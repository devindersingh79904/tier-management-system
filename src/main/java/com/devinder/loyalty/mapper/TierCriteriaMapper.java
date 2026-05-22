package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.response.TierCriteriaResponse;
import com.devinder.loyalty.entity.TierCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface TierCriteriaMapper {

    @Mapping(target = "membershipTierId", source = "membershipTier.id")
    @Mapping(target = "membershipTierName", source = "membershipTier.name")
    TierCriteriaResponse toResponse(TierCriteria entity);
}
