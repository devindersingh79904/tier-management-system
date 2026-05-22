package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.dto.request.CreatePaymentIntentRequest;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.PaymentIntent;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.enums.PaymentMethod;
import com.devinder.loyalty.enums.PaymentStatus;
import com.devinder.loyalty.enums.TransactionType;
import com.devinder.loyalty.enums.UserRole;
import com.devinder.loyalty.repository.MembershipEventRepository;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.PaymentIntentRepository;
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
public class UserPaymentIntentControllerTest {

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
    private PaymentIntentRepository paymentIntentRepository;

    @Autowired
    private MembershipEventRepository membershipEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User otherUser;
    private MembershipPlan testPlan;
    private MembershipTier testTier;
    private UserMembership testMembership;
    private UserMembership otherMembership;
    private PaymentIntent testPayment;

    private final String testMobile = "8888888802";
    private final String otherMobile = "8888888803";
    private final String testPlanName = "PAY_USER_PLAN";
    private final String testTierName = "PAY_USER_TIER";

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = User.builder()
                .name("Payment User Customer")
                .mobileNumber(testMobile)
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        otherUser = User.builder()
                .name("Other Customer")
                .mobileNumber(otherMobile)
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        otherUser = userRepository.save(otherUser);

        testPlan = MembershipPlan.builder()
                .name(testPlanName)
                .duration(1)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(2500L)
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
                .purchasedPrice(2500L)
                .discountAmount(0L)
                .finalPrice(2500L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        otherMembership = UserMembership.builder()
                .user(otherUser)
                .membershipPlan(testPlan)
                .membershipTier(testTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(Instant.now())
                .endDate(Instant.now().plusSeconds(3600))
                .purchasedPrice(2500L)
                .discountAmount(0L)
                .finalPrice(2500L)
                .autoRenew(true)
                .build();
        otherMembership = userMembershipRepository.save(otherMembership);

        testPayment = PaymentIntent.builder()
                .userMembership(testMembership)
                .transactionType(TransactionType.PAYMENT)
                .amount(2500L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("tx_user_123")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_user_123")
                .paymentMethod(PaymentMethod.UPI)
                .build();
        testPayment = paymentIntentRepository.save(testPayment);
    }

    @AfterEach
    @Transactional
    void cleanUp() {
        paymentIntentRepository.deleteAll();
        membershipEventRepository.deleteAll();

        userMembershipRepository.findAll().stream()
                .filter(m -> m.getMembershipPlan().getName().equals(testPlanName))
                .forEach(m -> userMembershipRepository.delete(m));

        membershipPlanRepository.findByName(testPlanName).ifPresent(p -> membershipPlanRepository.delete(p));
        membershipTierRepository.findByName(testTierName).ifPresent(t -> membershipTierRepository.delete(t));
        userRepository.findByMobileNumber(testMobile).ifPresent(u -> userRepository.delete(u));
        userRepository.findByMobileNumber(otherMobile).ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void createPayment_Success() throws Exception {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .userMembershipId(testMembership.getId())
                .transactionType(TransactionType.PAYMENT)
                .amount(2500L)
                .paymentMethod(PaymentMethod.UPI)
                .transactionId("tx_user_456")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_user_456")
                .build();

        mockMvc.perform(post("/api/v1/me/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.paymentStatus", is("SUCCESS")))
                .andExpect(jsonPath("$.data.amount", is(2500)));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void createPayment_DuplicateIdempotencyKey_ReturnsExisting() throws Exception {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .userMembershipId(testMembership.getId())
                .transactionType(TransactionType.PAYMENT)
                .amount(2500L)
                .paymentMethod(PaymentMethod.UPI)
                .transactionId("tx_user_999")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_user_123") // Matches setup's testPayment
                .build();

        mockMvc.perform(post("/api/v1/me/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.id", is(testPayment.getId())));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void createPayment_UnauthorizedMembership_ThrowsUnauthorized() throws Exception {
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .userMembershipId(otherMembership.getId()) // Belongs to otherUser
                .transactionType(TransactionType.PAYMENT)
                .amount(2500L)
                .paymentMethod(PaymentMethod.CARD)
                .transactionId("tx_user_789")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_user_789")
                .build();

        mockMvc.perform(post("/api/v1/me/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void createPayment_InactiveMembership_ThrowsConflict() throws Exception {
        testMembership.setStatus(MembershipStatus.CANCELLED);
        userMembershipRepository.save(testMembership);

        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .userMembershipId(testMembership.getId())
                .transactionType(TransactionType.PAYMENT)
                .amount(2500L)
                .paymentMethod(PaymentMethod.CARD)
                .transactionId("tx_user_333")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_user_333")
                .build();

        mockMvc.perform(post("/api/v1/me/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyPayments_Success() throws Exception {
        mockMvc.perform(get("/api/v1/me/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].id", is(testPayment.getId())));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyPaymentById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/me/payments/" + testPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(testPayment.getId())));
    }

    @Test
    @WithMockUser(username = testMobile, roles = {"USER"})
    void getMyPaymentById_Unauthorized_ForOtherUserPayment() throws Exception {
        // Create payment belonging to otherUser's membership
        PaymentIntent otherPayment = PaymentIntent.builder()
                .userMembership(otherMembership)
                .transactionType(TransactionType.PAYMENT)
                .amount(2500L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("tx_other_111")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_other_111")
                .paymentMethod(PaymentMethod.CARD)
                .build();
        otherPayment = paymentIntentRepository.save(otherPayment);

        mockMvc.perform(get("/api/v1/me/payments/" + otherPayment.getId()))
                .andExpect(status().isUnauthorized());
    }
}
