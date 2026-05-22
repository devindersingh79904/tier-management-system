package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.CreateTierCriteriaRequest;
import com.devinder.loyalty.dto.request.UpdateTierCriteriaRequest;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.TierCriteria;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.TierCriteriaRepository;
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
@Transactional
public class TierCriteriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private TierCriteriaRepository tierCriteriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MembershipTier testTier;

    private final String testTierName = "TEST_CRITERIA_TIER";

    @BeforeEach
    void setUp() {
        cleanUp();

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
        tierCriteriaRepository.findAll().stream()
                .filter(c -> c.getMembershipTier().getName().equals(testTierName))
                .forEach(c -> tierCriteriaRepository.delete(c));

        membershipTierRepository.findByName(testTierName).ifPresent(t -> membershipTierRepository.delete(t));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCriteria_Success_AsAdmin() throws Exception {
        CreateTierCriteriaRequest request = CreateTierCriteriaRequest.builder()
                .membershipTierId(testTier.getId())
                .criteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}")
                .build();

        mockMvc.perform(post("/api/v1/admin/tier-criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.criteriaJson", is("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}")))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCriteria_Success() throws Exception {
        TierCriteria criteria = TierCriteria.builder()
                .membershipTier(testTier)
                .criteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}")
                .isActive(true)
                .build();
        criteria = tierCriteriaRepository.save(criteria);

        UpdateTierCriteriaRequest request = UpdateTierCriteriaRequest.builder()
                .criteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 15000}")
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/admin/tier-criteria/" + criteria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.criteriaJson", is("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 15000}")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getCriteriaById_Success() throws Exception {
        TierCriteria criteria = TierCriteria.builder()
                .membershipTier(testTier)
                .criteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}")
                .isActive(true)
                .build();
        criteria = tierCriteriaRepository.save(criteria);

        mockMvc.perform(get("/api/v1/admin/tier-criteria/" + criteria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(criteria.getId())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllCriteria_Success() throws Exception {
        TierCriteria criteria = TierCriteria.builder()
                .membershipTier(testTier)
                .criteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}")
                .isActive(true)
                .build();
        tierCriteriaRepository.save(criteria);

        mockMvc.perform(get("/api/v1/admin/tier-criteria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCriteria_Success() throws Exception {
        TierCriteria criteria = TierCriteria.builder()
                .membershipTier(testTier)
                .criteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}")
                .isActive(true)
                .build();
        criteria = tierCriteriaRepository.save(criteria);

        mockMvc.perform(delete("/api/v1/admin/tier-criteria/" + criteria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<TierCriteria> deleted = tierCriteriaRepository.findById(criteria.getId());
        assertTrue(deleted.isPresent());
        assertFalse(deleted.get().getIsActive());
    }
}
