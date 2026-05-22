package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.RefundPaymentRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminPaymentIntentControllerTest {

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
    private MembershipPlan testPlan;
    private MembershipTier testTier;
    private UserMembership testMembership;
    private PaymentIntent testPayment;

    private final String testMobile = "7777777702";
    private final String testPlanName = "PAY_ADMIN_PLAN";
    private final String testTierName = "PAY_ADMIN_TIER";

    @BeforeEach
    void setUp() {
        cleanUp();

        testUser = User.builder()
                .name("Payment Admin User")
                .mobileNumber(testMobile)
                .passwordHash("password")
                .role(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        testPlan = MembershipPlan.builder()
                .name(testPlanName)
                .duration(1)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(1200L)
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
                .purchasedPrice(1200L)
                .discountAmount(0L)
                .finalPrice(1200L)
                .autoRenew(true)
                .build();
        testMembership = userMembershipRepository.save(testMembership);

        testPayment = PaymentIntent.builder()
                .userMembership(testMembership)
                .transactionType(TransactionType.PAYMENT)
                .amount(1200L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("tx_admin_123")
                .paymentProvider("STRIPE")
                .idempotencyKey("idem_admin_123")
                .paymentMethod(PaymentMethod.CARD)
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
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllPayments_Success_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllPayments_Forbidden_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments/" + testPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(testPayment.getId())))
                .andExpect(jsonPath("$.data.paymentStatus", is("SUCCESS")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void refundPayment_Success() throws Exception {
        RefundPaymentRequest request = new RefundPaymentRequest();
        request.setReason("Billing error");

        mockMvc.perform(put("/api/v1/admin/payments/" + testPayment.getId() + "/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.paymentStatus", is("REFUNDED")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void refundPayment_AlreadyRefunded_ThrowsConflict() throws Exception {
        testPayment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentIntentRepository.save(testPayment);

        RefundPaymentRequest request = new RefundPaymentRequest();
        request.setReason("Double refund");

        mockMvc.perform(put("/api/v1/admin/payments/" + testPayment.getId() + "/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
