package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.enums.MembershipEventType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminMembershipEventControllerTest {

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
    private MembershipEvent testEvent;

    private final String testMobile = "7777777701";
    private final String testPlanName = "EVENT_ADMIN_PLAN";
    private final String testTierName = "EVENT_ADMIN_TIER";

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = User.builder()
                .name("Event Admin User")
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

        testTier = MembershipTier.builder()
                .name(testTierName)
                .priority(1)
                .isActive(true)
                .build();
        testTier = membershipTierRepository.save(testTier);

        testMembership = UserMembership.builder()
                .user(testUser)
                .membershipPlan(testPlan)
                .membershipTier(testTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(1000L)
                .discountAmount(0L)
                .finalPrice(1000L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        testEvent = MembershipEvent.builder()
                .userMembership(testMembership)
                .eventType(MembershipEventType.SUBSCRIBED)
                .oldValue(null)
                .newValue("ACTIVE")
                .reason("Initial subscription")
                .build();
        testEvent = membershipEventRepository.save(testEvent);
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllEvents_Success_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/membership-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllEvents_Forbidden_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/membership-events"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEvents_Unauthorized_Anonymous() throws Exception {
        mockMvc.perform(get("/api/v1/admin/membership-events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getEventById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/membership-events/" + testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(testEvent.getId())))
                .andExpect(jsonPath("$.data.eventType", is("SUBSCRIBED")))
                .andExpect(jsonPath("$.data.newValue", is("ACTIVE")))
                .andExpect(jsonPath("$.data.reason", is("Initial subscription")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getEventById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/membership-events/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getEventsByMembershipId_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/membership-events/membership/" + testMembership.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].userMembershipId", is(testMembership.getId())));
    }
}
