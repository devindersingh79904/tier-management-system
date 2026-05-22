package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.response.BenefitConfigurationResponse;
import com.devinder.loyalty.entity.BenefitConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface BenefitConfigurationMapper {

    @Mapping(target = "membershipBenefitId", source = "membershipBenefit.id")
    @Mapping(target = "membershipBenefitName", source = "membershipBenefit.name")
    @Mapping(target = "membershipPlanId", source = "membershipPlan.id")
    @Mapping(target = "membershipPlanName", source = "membershipPlan.name")
    @Mapping(target = "membershipTierId", source = "membershipTier.id")
    @Mapping(target = "membershipTierName", source = "membershipTier.name")
    BenefitConfigurationResponse toResponse(BenefitConfiguration entity);
}
