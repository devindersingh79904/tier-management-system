package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.enums.UserRole;
import com.devinder.loyalty.repository.MembershipEventRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private UserMembershipRepository userMembershipRepository;

    @Autowired
    private MembershipEventRepository membershipEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private MembershipPlan testPlan;
    private MembershipTier testTier;
    private UserMembership testMembership;

    private final String testMobile = "8888888888";
    private final String testPlanName = "TEST_USER_PLAN";
    private final String testTierName = "TEST_USER_TIER";

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = User.builder()
                .name("User Test Customer")
                .mobileNumber(testMobile)
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        testPlan = MembershipPlan.builder()
                .name(testPlanName)
                .duration(1)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(2000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        testPlan = membershipPlanRepository.save(testPlan);

        testTier = MembershipTier.builder()
                .name(testTierName)
                .priority(1)
                .isActive(true)
                .build();
        testTier = membershipTierRepository.save(testTier);
    }

    @AfterEach
    @Transactional
    void cleanUp() {
        membershipEventRepository.deleteAll();

        userMembershipRepository.findAll().stream()
                .filter(m -> m.getMembershipPlan().getName().equals(testPlanName))
                .forEach(m -> userMembershipRepository.delete(m));

        membershipPlanRepository.findByName(testPlanName).ifPresent(p -> membershipPlanRepository.delete(p));
        membershipTierRepository.findByName(testTierName).ifPresent(t -> membershipTierRepository.delete(t));
        userRepository.findByMobileNumber(testMobile).ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void subscribe_Success() throws Exception {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .membershipPlanId(testPlan.getId())
                .membershipTierId(testTier.getId())
                .autoRenew(true)
                .build();

        mockMvc.perform(post("/api/v1/me/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.userMobileNumber", is(testMobile)));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyActiveMembership_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(2000L)
                .discountAmount(0L)
                .finalPrice(2000L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        mockMvc.perform(get("/api/v1/me/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(testMembership.getId())));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyActiveMembership_NotFound_WhenNone() throws Exception {
        mockMvc.perform(get("/api/v1/me/memberships"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyMembershipsHistory_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(2000L)
                .discountAmount(0L)
                .finalPrice(2000L)
                .autoRenew(true)
                .build();
        userMembershipRepository.save(testMembership);

        mockMvc.perform(get("/api/v1/me/memberships/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyResolvedBenefits_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(2000L)
                .discountAmount(0L)
                .finalPrice(2000L)
                .autoRenew(true)
                .build();
        userMembershipRepository.save(testMembership);

        mockMvc.perform(get("/api/v1/me/memberships/benefits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)));
    }
}
