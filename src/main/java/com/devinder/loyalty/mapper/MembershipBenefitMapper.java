package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.request.CreateMembershipBenefitRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipBenefitRequest;
import com.devinder.loyalty.dto.response.MembershipBenefitResponse;
import com.devinder.loyalty.entity.MembershipBenefit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MembershipBenefitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    MembershipBenefit toEntity(CreateMembershipBenefitRequest request);

    MembershipBenefitResponse toResponse(MembershipBenefit entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateMembershipBenefitRequest request, @MappingTarget MembershipBenefit entity);
}
