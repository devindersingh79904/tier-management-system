package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.response.PaymentIntentResponse;
import com.devinder.loyalty.entity.PaymentIntent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PaymentIntentMapper {

    @Mapping(target = "userMembershipId", source = "userMembership.id")
    PaymentIntentResponse toResponse(PaymentIntent entity);
}
