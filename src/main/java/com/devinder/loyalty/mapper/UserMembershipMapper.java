package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.entity.UserMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMembershipMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userMobileNumber", source = "user.mobileNumber")
    @Mapping(target = "membershipPlanId", source = "membershipPlan.id")
    @Mapping(target = "membershipPlanName", source = "membershipPlan.name")
    @Mapping(target = "membershipTierId", source = "membershipTier.id")
    @Mapping(target = "membershipTierName", source = "membershipTier.name")
    UserMembershipResponse toResponse(UserMembership entity);
}
