package com.devinder.loyalty.dto.response;

import com.devinder.loyalty.enums.MembershipEventType;
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
@Schema(description = "Membership event log response payload")
public class MembershipEventResponse {

    @Schema(description = "UUID of the membership event record", example = "92bf88e1-561b-4f81-a67b-12d8376483fb")
    private String id;

    @Schema(description = "UUID of the user membership association", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e")
    private String userMembershipId;

    @Schema(description = "Type of the membership event", example = "SUBSCRIBED")
    private MembershipEventType eventType;

    @Schema(description = "Pre-event state value", example = "NONE")
    private String oldValue;

    @Schema(description = "Post-event state value", example = "GOLD")
    private String newValue;

    @Schema(description = "Reason or trigger explanation for this state transition", example = "Initial user subscription")
    private String reason;

    @Schema(description = "Timestamp when the event occurred", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the event was last updated", example = "2026-05-21T18:00:00Z")
    private Instant updatedAt;
}
