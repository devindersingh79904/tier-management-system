package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserMembershipPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    private final String testPlanName1 = "TEST_PLAN_USER_ACTIVE";
    private final String testPlanName2 = "TEST_PLAN_USER_INACTIVE";

    @BeforeEach
    @AfterEach
    void cleanUp() {
        membershipPlanRepository.findAll().stream()
                .filter(p -> p.getName().startsWith("TEST_PLAN_USER_"))
                .forEach(p -> membershipPlanRepository.delete(p));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getActivePlans_Success() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        membershipPlanRepository.save(plan);

        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getActivePlanById_Success() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan = membershipPlanRepository.save(plan);

        mockMvc.perform(get("/api/v1/plans/" + plan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.name", is(testPlanName1)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getActivePlanById_InactivePlan_ReturnsNotFound() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName2)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(false)
                .build();
        plan = membershipPlanRepository.save(plan);

        mockMvc.perform(get("/api/v1/plans/" + plan.getId()))
                .andExpect(status().isNotFound());
    }
}
