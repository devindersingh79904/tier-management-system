package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.CreateMembershipBenefitRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipBenefitRequest;
import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.repository.MembershipBenefitRepository;
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
public class MembershipBenefitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipBenefitRepository membershipBenefitRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testBenefitName1 = "TEST_BENEFIT_SHIPPING";
    private final String testBenefitName2 = "TEST_BENEFIT_CASHBACK";

    @BeforeEach
    @AfterEach
    void cleanUp() {
        membershipBenefitRepository.findAll().stream()
                .filter(b -> b.getName().startsWith("TEST_BENEFIT_"))
                .forEach(b -> membershipBenefitRepository.delete(b));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createBenefit_Success_AsAdmin() throws Exception {
        CreateMembershipBenefitRequest request = CreateMembershipBenefitRequest.builder()
                .name(testBenefitName1)
                .description("Test shipping benefit")
                .build();

        mockMvc.perform(post("/api/v1/admin/benefits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.name", is(testBenefitName1)))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createBenefit_Forbidden_AsUser() throws Exception {
        CreateMembershipBenefitRequest request = CreateMembershipBenefitRequest.builder()
                .name(testBenefitName1)
                .description("Test shipping benefit")
                .build();

        mockMvc.perform(post("/api/v1/admin/benefits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateBenefit_Success() throws Exception {
        MembershipBenefit benefit = MembershipBenefit.builder()
                .name(testBenefitName1)
                .description("Test description")
                .isActive(true)
                .build();
        benefit = membershipBenefitRepository.save(benefit);

        UpdateMembershipBenefitRequest request = UpdateMembershipBenefitRequest.builder()
                .name(testBenefitName2)
                .description("Updated description")
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/admin/benefits/" + benefit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.name", is(testBenefitName2)))
                .andExpect(jsonPath("$.data.description", is("Updated description")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getBenefitById_Success() throws Exception {
        MembershipBenefit benefit = MembershipBenefit.builder()
                .name(testBenefitName1)
                .description("Test description")
                .isActive(true)
                .build();
        benefit = membershipBenefitRepository.save(benefit);

        mockMvc.perform(get("/api/v1/admin/benefits/" + benefit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.name", is(testBenefitName1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllBenefits_Success() throws Exception {
        MembershipBenefit benefit = MembershipBenefit.builder()
                .name(testBenefitName1)
                .description("Test description")
                .isActive(true)
                .build();
        membershipBenefitRepository.save(benefit);

        mockMvc.perform(get("/api/v1/admin/benefits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteBenefit_Success() throws Exception {
        MembershipBenefit benefit = MembershipBenefit.builder()
                .name(testBenefitName1)
                .description("Test description")
                .isActive(true)
                .build();
        benefit = membershipBenefitRepository.save(benefit);

        mockMvc.perform(delete("/api/v1/admin/benefits/" + benefit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<MembershipBenefit> deleted = membershipBenefitRepository.findById(benefit.getId());
        assertTrue(deleted.isPresent());
        assertFalse(deleted.get().getIsActive());
    }
}
