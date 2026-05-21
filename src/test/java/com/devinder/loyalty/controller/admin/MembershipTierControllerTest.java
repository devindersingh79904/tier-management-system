package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.CreateMembershipTierRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipTierRequest;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.enums.UserRole;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MembershipTierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private UserMembershipRepository userMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testTierName1 = "TEST_TIER_GOLD";
    private final String testTierName2 = "TEST_TIER_SILVER";
    private final String testMobile = "9898989898";
    private final String testPlanName = "TEST_PLAN";

    @BeforeEach
    @AfterEach
    @org.springframework.transaction.annotation.Transactional
    void cleanUp() {
        // Delete test memberships
        userMembershipRepository.findByMembershipTierNameStartingWith("TEST_TIER")
                .forEach(m -> userMembershipRepository.delete(m));

        // Delete test tiers
        membershipTierRepository.findByName(testTierName1).ifPresent(t -> membershipTierRepository.delete(t));
        membershipTierRepository.findByName(testTierName2).ifPresent(t -> membershipTierRepository.delete(t));

        // Delete test plans
        membershipPlanRepository.findAll().stream()
                .filter(p -> p.getName().equals(testPlanName))
                .forEach(p -> membershipPlanRepository.delete(p));

        // Delete test users
        userRepository.findByMobileNumber(testMobile).ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createTier_Success_AsAdmin() throws Exception {
        CreateMembershipTierRequest request = CreateMembershipTierRequest.builder()
                .name(testTierName1)
                .priority(99)
                .description("Test admin description")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/admin/tiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.name", is(testTierName1)))
                .andExpect(jsonPath("$.data.priority", is(99)))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createTier_Forbidden_AsUser() throws Exception {
        CreateMembershipTierRequest request = CreateMembershipTierRequest.builder()
                .name(testTierName1)
                .priority(99)
                .build();

        mockMvc.perform(post("/api/v1/admin/tiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.errors[0]", is("ERR_FORBIDDEN")));
    }

    @Test
    void createTier_Unauthorized_Unauthenticated() throws Exception {
        CreateMembershipTierRequest request = CreateMembershipTierRequest.builder()
                .name(testTierName1)
                .priority(99)
                .build();

        mockMvc.perform(post("/api/v1/admin/tiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.errors[0]", is("ERR_UNAUTHORIZED")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createTier_DuplicateName_Conflict() throws Exception {
        MembershipTier existing = MembershipTier.builder()
                .name(testTierName1)
                .priority(98)
                .isActive(true)
                .build();
        membershipTierRepository.save(existing);

        CreateMembershipTierRequest request = CreateMembershipTierRequest.builder()
                .name(testTierName1)
                .priority(99)
                .build();

        mockMvc.perform(post("/api/v1/admin/tiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.errors[0]", is("ERR_CONFLICT")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createTier_DuplicatePriority_Conflict() throws Exception {
        MembershipTier existing = MembershipTier.builder()
                .name(testTierName2)
                .priority(99)
                .isActive(true)
                .build();
        membershipTierRepository.save(existing);

        CreateMembershipTierRequest request = CreateMembershipTierRequest.builder()
                .name(testTierName1)
                .priority(99)
                .build();

        mockMvc.perform(post("/api/v1/admin/tiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.errors[0]", is("ERR_CONFLICT")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateTier_Success() throws Exception {
        MembershipTier existing = MembershipTier.builder()
                .name(testTierName1)
                .priority(99)
                .isActive(true)
                .build();
        existing = membershipTierRepository.save(existing);

        UpdateMembershipTierRequest request = UpdateMembershipTierRequest.builder()
                .name(testTierName1)
                .priority(99)
                .description("Updated Description")
                .isActive(false)
                .build();

        mockMvc.perform(put("/api/v1/admin/tiers/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.description", is("Updated Description")))
                .andExpect(jsonPath("$.data.isActive", is(false)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getTierById_Success_AsUser() throws Exception {
        MembershipTier existing = MembershipTier.builder()
                .name(testTierName1)
                .priority(99)
                .isActive(true)
                .build();
        existing = membershipTierRepository.save(existing);

        mockMvc.perform(get("/api/v1/tiers/" + existing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.name", is(testTierName1)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllTiers_Success_AsUser() throws Exception {
        MembershipTier existing = MembershipTier.builder()
                .name(testTierName1)
                .priority(99)
                .isActive(true)
                .build();
        membershipTierRepository.save(existing);

        mockMvc.perform(get("/api/v1/tiers")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "priority,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteTier_Success() throws Exception {
        MembershipTier existing = MembershipTier.builder()
                .name(testTierName1)
                .priority(99)
                .isActive(true)
                .build();
        existing = membershipTierRepository.save(existing);

        mockMvc.perform(delete("/api/v1/admin/tiers/" + existing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<MembershipTier> deleted = membershipTierRepository.findById(existing.getId());
        assertTrue(deleted.isPresent());
        assertFalse(deleted.get().getIsActive());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteTier_ActiveMembershipsExist_Conflict() throws Exception {
        // Create test tier
        MembershipTier tier = MembershipTier.builder()
                .name(testTierName1)
                .priority(99)
                .isActive(true)
                .build();
        tier = membershipTierRepository.save(tier);

        // Create test plan
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName)
                .duration(1)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(1000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan = membershipPlanRepository.save(plan);

        // Create test user
        User user = User.builder()
                .name("Test User Delete")
                .mobileNumber(testMobile)
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        // Create active membership
        UserMembership membership = UserMembership.builder()
                .user(user)
                .membershipPlan(plan)
                .membershipTier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(1000L)
                .discountAmount(0L)
                .finalPrice(1000L)
                .autoRenew(true)
                .build();
        userMembershipRepository.save(membership);

        // Attempt soft-deleting tier
        mockMvc.perform(delete("/api/v1/admin/tiers/" + tier.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Cannot deactivate tier because active memberships exist")));
    }
}
