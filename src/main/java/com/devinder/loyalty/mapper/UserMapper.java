package com.devinder.loyalty.mapper;

import com.devinder.loyalty.dto.response.UserProfileResponse;
import com.devinder.loyalty.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMapper {
    UserProfileResponse toResponse(User entity);
}
