- [x] Analyze existing codebase structure and patterns
- [x] 1. Downgrade support - Refactored UserMembershipServiceImpl with reusable changeTier() method
- [x] 2. User Self-Service APIs - Added upgrade/downgrade/cancel endpoints + ownership validation
- [x] 3. Order domain - Order entity, OrderStatus enum, OrderRepository with aggregate queries
- [x] 4. Automatic tier evaluation - evaluateTiers() in service using existing TierEvaluationService + @Scheduled
- [x] 5. Membership expiry scheduler - MembershipScheduler with @Scheduled on expireMemberships()
- [x] 6. Auto-renewal - autoRenewMemberships() in service + @Scheduled for daily renewal
- [x] 7. Benefit application engine - DiscountCalculator interface + impl, BenefitResolutionResult DTO
- [x] 8. Cohort support - cohort in buildEvaluationContext() + tier evaluation context
- [x] 9. Swagger/OpenAPI - All new endpoints documented with @Operation, @ApiResponses, @Tag
- [x] Build passing - 144 source files compile, 137 tests pass, 0 Checkstyle violations

BUGS FOUND DURING AUDIT:
- [ ] BUG: TierCriteria seeder uses `"condition"` field but TierEvaluationService reads `"operator"` — criteria NEVER match
- [ ] BUG: Seeder uses `min_orders`, `min_spend` but context uses `orderCount`, `totalOrderValue` — field names mismatch
- [ ] BUG: No Monthly/Quarterly/Yearly seeding — assignment requires all three
- [ ] BUG: No cohort-based criteria in seeder
- [ ] BUG: Silver criteria `min_orders >= 0` is always true (and field name mismatch anyway)
- [ ] Fix: Add cohort-based criteria to TierCriteriaSeeder
- [ ] Fix: Regenerate tests for new features