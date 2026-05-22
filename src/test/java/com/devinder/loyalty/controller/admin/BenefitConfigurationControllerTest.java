package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.dto.request.CreateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.request.UpdateBenefitConfigurationRequest;
import com.devinder.loyalty.entity.BenefitConfiguration;
import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.repository.BenefitConfigurationRepository;
import com.devinder.loyalty.repository.MembershipBenefitRepository;
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
public class BenefitConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private MembershipBenefitRepository membershipBenefitRepository;

    @Autowired
    private BenefitConfigurationRepository benefitConfigurationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MembershipPlan testPlan;
    private MembershipBenefit testBenefit;

    private final String testPlanName = "TEST_CONFIG_PLAN";
    private final String testBenefitName = "TEST_CONFIG_BENEFIT";

    @BeforeEach
    void setUp() {
        cleanUp();

        testPlan = MembershipPlan.builder()
                .name(testPlanName)
                .duration(1)
                .durationUnit(DurationUnit.MONTH)
                .basePrice(5000L)
                .currency(CurrencyType.INR)
                .isActive(true)
                .build();
        testPlan = membershipPlanRepository.save(testPlan);

        testBenefit = MembershipBenefit.builder()
                .name(testBenefitName)
                .description("Test description")
                .isActive(true)
                .build();
        testBenefit = membershipBenefitRepository.save(testBenefit);
    }

    @AfterEach
    @Transactional
    void cleanUp() {
        benefitConfigurationRepository.findAll().stream()
                .filter(c -> c.getMembershipBenefit().getName().equals(testBenefitName))
                .forEach(c -> benefitConfigurationRepository.delete(c));

        membershipPlanRepository.findByName(testPlanName).ifPresent(p -> membershipPlanRepository.delete(p));
        membershipBenefitRepository.findByName(testBenefitName).ifPresent(b -> membershipBenefitRepository.delete(b));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createConfiguration_Success_AsAdmin() throws Exception {
        CreateBenefitConfigurationRequest request = CreateBenefitConfigurationRequest.builder()
                .membershipBenefitId(testBenefit.getId())
                .membershipPlanId(testPlan.getId())
                .configurationJson("{\"discount\": 10}")
                .build();

        mockMvc.perform(post("/api/v1/admin/benefit-configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.configurationJson", is("{\"discount\": 10}")))
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateConfiguration_Success() throws Exception {
        BenefitConfiguration config = BenefitConfiguration.builder()
                .membershipBenefit(testBenefit)
                .membershipPlan(testPlan)
                .configurationJson("{\"discount\": 10}")
                .isActive(true)
                .build();
        config = benefitConfigurationRepository.save(config);

        UpdateBenefitConfigurationRequest request = UpdateBenefitConfigurationRequest.builder()
                .configurationJson("{\"discount\": 15}")
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/admin/benefit-configurations/" + config.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.configurationJson", is("{\"discount\": 15}")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getConfigurationById_Success() throws Exception {
        BenefitConfiguration config = BenefitConfiguration.builder()
                .membershipBenefit(testBenefit)
                .membershipPlan(testPlan)
                .configurationJson("{\"discount\": 10}")
                .isActive(true)
                .build();
        config = benefitConfigurationRepository.save(config);

        mockMvc.perform(get("/api/v1/admin/benefit-configurations/" + config.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(config.getId())));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllConfigurations_Success() throws Exception {
        BenefitConfiguration config = BenefitConfiguration.builder()
                .membershipBenefit(testBenefit)
                .membershipPlan(testPlan)
                .configurationJson("{\"discount\": 10}")
                .isActive(true)
                .build();
        benefitConfigurationRepository.save(config);

        mockMvc.perform(get("/api/v1/admin/benefit-configurations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteConfiguration_Success() throws Exception {
        BenefitConfiguration config = BenefitConfiguration.builder()
                .membershipBenefit(testBenefit)
                .membershipPlan(testPlan)
                .configurationJson("{\"discount\": 10}")
                .isActive(true)
                .build();
        config = benefitConfigurationRepository.save(config);

        mockMvc.perform(delete("/api/v1/admin/benefit-configurations/" + config.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<BenefitConfiguration> deleted = benefitConfigurationRepository.findById(config.getId());
        assertTrue(deleted.isPresent());
        assertFalse(deleted.get().getIsActive());
    }
}
