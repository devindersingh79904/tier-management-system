package com.devinder.loyalty.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageConstants {
    public static final String SERVER_WORKING = "Server is working";
    public static final String SUCCESS = "Operation completed successfully";
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String VALIDATION_ERROR = "Input validation failed. Please check details.";

    // Authentication operations
    public static final String SIGNUP_SUCCESS = "User registered successfully";
    public static final String LOGIN_SUCCESS = "User logged in successfully";

    // Loyalty Tier operations
    public static final String TIER_CREATED = "Loyalty tier created successfully";
    public static final String TIER_UPDATED = "Loyalty tier updated successfully";
    public static final String TIER_DELETED = "Loyalty tier deleted successfully";

    // Membership Benefit operations
    public static final String BENEFIT_CREATED = "Membership benefit created successfully";
    public static final String BENEFIT_UPDATED = "Membership benefit updated successfully";
    public static final String BENEFIT_DELETED = "Membership benefit deleted successfully";
    public static final String BENEFIT_NAME_EXISTS = "Membership benefit with name '%s' already exists";
    public static final String BENEFIT_ID_NOT_FOUND = "Membership benefit with ID %s not found";
    public static final String BENEFIT_HAS_ACTIVE_CONFIGURATIONS = "Cannot deactivate/delete benefit because active configurations exist";

    // Exception / Concurrency messages
    public static final String CONFLICT_LOCKING = "The resource was updated by another process. Used optimistic locking to avoid concurrent subscription update conflicts.";
    public static final String TIER_NAME_EXISTS = "Loyalty tier with name '%s' already exists";
    public static final String TIER_ID_NOT_FOUND = "Loyalty tier with ID %s not found";
    public static final String TIER_NAME_NOT_FOUND = "Loyalty tier with name '%s' not found";
    public static final String TIER_PRIORITY_EXISTS = "Loyalty tier with priority '%d' already exists";
    public static final String TIER_HAS_ACTIVE_MEMBERSHIPS = "Cannot deactivate tier because active memberships exist";

    // Validation messages
    public static final String VAL_TIER_NAME_REQUIRED = "Tier name is required";
    public static final String VAL_TIER_NAME_LENGTH = "Tier name must not exceed 50 characters";
    public static final String VAL_TIER_LEVEL_REQUIRED = "Tier level is required";
    public static final String VAL_TIER_POINTS_REQUIRED = "Minimum points is required";
    public static final String VAL_TIER_POINTS_MIN = "Minimum points must be 0 or positive";
    public static final String VAL_TIER_DESC_LENGTH = "Description must not exceed 255 characters";
}
