package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.entity.MembershipPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MembershipPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    MembershipPlan toEntity(CreateMembershipPlanRequest request);

    MembershipPlanResponse toResponse(MembershipPlan entity);
}
