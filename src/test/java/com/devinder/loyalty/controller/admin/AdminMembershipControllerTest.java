package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.CancelMembershipRequest;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.request.UpgradeMembershipRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminMembershipControllerTest {

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
    private MembershipTier testTierSilver;
    private MembershipTier testTierGold;
    private UserMembership testMembership;

    private final String testMobile = "7777777777";
    private final String testPlanName = "TEST_MEMB_PLAN";
    private final String testTierSilverName = "TEST_TIER_SILVER_MEMB";
    private final String testTierGoldName = "TEST_TIER_GOLD_MEMB";

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = User.builder()
                .name("Admin Test User")
                .mobileNumber(testMobile)
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        testPlan = MembershipPlan.builder()
                .name(testPlanName)
                .duration(1)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(1000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        testPlan = membershipPlanRepository.save(testPlan);

        testTierSilver = MembershipTier.builder()
                .name(testTierSilverName)
                .priority(1)
                .isActive(true)
                .build();
        testTierSilver = membershipTierRepository.save(testTierSilver);

        testTierGold = MembershipTier.builder()
                .name(testTierGoldName)
                .priority(2)
                .isActive(true)
                .build();
        testTierGold = membershipTierRepository.save(testTierGold);
    }

    @AfterEach
    @Transactional
    void cleanUp() {
        membershipEventRepository.deleteAll();
        
        userMembershipRepository.findAll().stream()
                .filter(m -> m.getMembershipPlan().getName().equals(testPlanName))
                .forEach(m -> userMembershipRepository.delete(m));

        membershipPlanRepository.findByName(testPlanName).ifPresent(p -> membershipPlanRepository.delete(p));
        membershipTierRepository.findByName(testTierSilverName).ifPresent(t -> membershipTierRepository.delete(t));
        membershipTierRepository.findByName(testTierGoldName).ifPresent(t -> membershipTierRepository.delete(t));
        userRepository.findByMobileNumber(testMobile).ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createMembership_Success_AsAdmin() throws Exception {
        CreateUserMembershipRequest request = CreateUserMembershipRequest.builder()
                .userId(testUser.getId())
                .membershipPlanId(testPlan.getId())
                .membershipTierId(testTierSilver.getId())
                .autoRenew(true)
                .build();

        mockMvc.perform(post("/api/v1/admin/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.userName", is(testUser.getName())))
                .andExpect(jsonPath("$.data.membershipPlanName", is(testPlanName)))
                .andExpect(jsonPath("$.data.membershipTierName", is(testTierSilverName)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getMembershipById_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTierSilver)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(1000L)
                .discountAmount(0L)
                .finalPrice(1000L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        mockMvc.perform(get("/api/v1/admin/memberships/" + testMembership.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(testMembership.getId())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllMemberships_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTierSilver)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(1000L)
                .discountAmount(0L)
                .finalPrice(1000L)
                .autoRenew(true)
                .build();
        userMembershipRepository.save(testMembership);

        mockMvc.perform(get("/api/v1/admin/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void upgradeMembership_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTierSilver)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(1000L)
                .discountAmount(0L)
                .finalPrice(1000L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        UpgradeMembershipRequest request = UpgradeMembershipRequest.builder()
                .membershipTierId(testTierGold.getId())
                .build();

        mockMvc.perform(put("/api/v1/admin/memberships/" + testMembership.getId() + "/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.membershipTierName", is(testTierGoldName)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void cancelMembership_Success() throws Exception {
        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTierSilver)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(1000L)
                .discountAmount(0L)
                .finalPrice(1000L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        CancelMembershipRequest request = CancelMembershipRequest.builder()
                .reason("Customer request")
                .build();

        mockMvc.perform(put("/api/v1/admin/memberships/" + testMembership.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }
}
