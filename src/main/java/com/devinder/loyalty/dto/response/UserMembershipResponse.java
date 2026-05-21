package com.devinder.loyalty.dto.response;

import com.devinder.loyalty.enums.MembershipStatus;
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
@Schema(description = "User membership details response payload")
public class UserMembershipResponse {

    @Schema(description = "UUID of the user membership", example = "92bf88e1-561b-4f81-a67b-12d8376483fb")
    private String id;

    @Schema(description = "UUID of the user", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private String userId;

    @Schema(description = "Name of the user", example = "Jane Doe")
    private String userName;

    @Schema(description = "Mobile number of the user", example = "9876543210")
    private String userMobileNumber;

    @Schema(description = "UUID of the membership plan", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e")
    private String membershipPlanId;

    @Schema(description = "Name of the membership plan", example = "Yearly Premium")
    private String membershipPlanName;

    @Schema(description = "UUID of the membership tier", example = "a2c16e78-bc5a-4712-8822-79015c92c55b")
    private String membershipTierId;

    @Schema(description = "Name of the membership tier", example = "GOLD")
    private String membershipTierName;

    @Schema(description = "Status of the membership (ACTIVE, EXPIRED, CANCELLED)", example = "ACTIVE")
    private MembershipStatus status;

    @Schema(description = "Membership starting timestamp", example = "2026-05-21T18:00:00Z")
    private Instant startDate;

    @Schema(description = "Membership ending timestamp", example = "2027-05-21T18:00:00Z")
    private Instant endDate;

    @Schema(description = "Base purchased price in paise/cents", example = "9900")
    private Long purchasedPrice;

    @Schema(description = "Discount amount applied in paise/cents", example = "900")
    private Long discountAmount;

    @Schema(description = "Final price paid in paise/cents", example = "9000")
    private Long finalPrice;

    @Schema(description = "Auto renew subscription status flag", example = "true")
    private Boolean autoRenew;

    @Schema(description = "Timestamp when the membership record was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the membership record was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
