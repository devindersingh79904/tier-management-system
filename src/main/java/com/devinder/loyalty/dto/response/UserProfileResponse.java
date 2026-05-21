package com.devinder.loyalty.dto.response;

import com.devinder.loyalty.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile details response payload")
public class UserProfileResponse {

    @Schema(description = "UUID of the user", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private String id;

    @Schema(description = "Name of the user", example = "Jane Doe")
    private String name;

    @Schema(description = "Mobile number of the user (primary identifier)", example = "9876543210")
    private String mobileNumber;

    @Schema(description = "Role of the user (USER, ADMIN, SUPER_ADMIN)", example = "USER")
    private UserRole role;

    @Schema(description = "Cohort code for targeting", example = "DEFAULT")
    private String cohort;

    @Schema(description = "Timestamp when the user account was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the user account was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
