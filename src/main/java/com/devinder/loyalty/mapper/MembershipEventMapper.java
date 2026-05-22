package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.response.MembershipEventResponse;
import com.devinder.loyalty.entity.MembershipEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MembershipEventMapper {

    @Mapping(target = "userMembershipId", source = "userMembership.id")
    MembershipEventResponse toResponse(MembershipEvent entity);
}
