package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipPlanRequest;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.repository.MembershipPlanRepository;
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
public class AdminMembershipPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testPlanName1 = "TEST_PLAN_GOLD_ANNUAL";
    private final String testPlanName2 = "TEST_PLAN_SILVER_MONTHLY";

    @BeforeEach
    @AfterEach
    void cleanUp() {
        membershipPlanRepository.findAll().stream()
                .filter(p -> p.getName().startsWith("TEST_PLAN_"))
                .forEach(p -> membershipPlanRepository.delete(p));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createPlan_Success_AsAdmin() throws Exception {
        CreateMembershipPlanRequest request = CreateMembershipPlanRequest.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.name", is(testPlanName1)))
                .andExpect(jsonPath("$.data.basePrice", is(12000)))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createPlan_Forbidden_AsUser() throws Exception {
        CreateMembershipPlanRequest request = CreateMembershipPlanRequest.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updatePlan_Success() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan = membershipPlanRepository.save(plan);

        UpdateMembershipPlanRequest request = UpdateMembershipPlanRequest.builder()
                .name(testPlanName2)
                .duration(6)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(7500L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/admin/plans/" + plan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.name", is(testPlanName2)))
                .andExpect(jsonPath("$.data.duration", is(6)))
                .andExpect(jsonPath("$.data.basePrice", is(7500)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPlanById_Success() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan = membershipPlanRepository.save(plan);

        mockMvc.perform(get("/api/v1/admin/plans/" + plan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.name", is(testPlanName1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllPlans_Success() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        membershipPlanRepository.save(plan);

        mockMvc.perform(get("/api/v1/admin/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deletePlan_Success() throws Exception {
        MembershipPlan plan = MembershipPlan.builder()
                .name(testPlanName1)
                .duration(1)
                .durationUnit(DurationUnit.YEAR)
                .basePrice(12000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        plan = membershipPlanRepository.save(plan);

        mockMvc.perform(delete("/api/v1/admin/plans/" + plan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<MembershipPlan> deleted = membershipPlanRepository.findById(plan.getId());
        assertTrue(deleted.isPresent());
        assertFalse(deleted.get().getIsActive());
    }
}
