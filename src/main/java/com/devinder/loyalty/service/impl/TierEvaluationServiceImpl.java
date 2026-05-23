package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.entity.TierCriteria;
import com.devinder.loyalty.repository.TierCriteriaRepository;
import com.devinder.loyalty.service.TierEvaluationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TierEvaluationServiceImpl implements TierEvaluationService {

    private final TierCriteriaRepository tierCriteriaRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public boolean evaluateEligibility(String tierId, Map<String, Object> contextParameters) {
        log.info("Evaluating eligibility for tier ID: {} with context: {}", tierId, contextParameters);

        List<TierCriteria> criteriaList = tierCriteriaRepository.findByMembershipTierIdAndIsActiveTrue(tierId);
        if (criteriaList.isEmpty()) {
            log.info("No active eligibility criteria defined for tier ID: {}", tierId);
            return false;
        }

        // Evaluate all criteria sets with OR logic — any matching set is sufficient
        for (TierCriteria criteria : criteriaList) {
            try {
                JsonNode rootNode = OBJECT_MAPPER.readTree(criteria.getCriteriaJson());
                if (evaluateNode(rootNode, contextParameters)) {
                    log.info("Tier ID: {} eligibility criteria matched: {}", tierId, criteria.getCriteriaJson());
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to parse or evaluate criteria JSON for tier ID: {}", tierId, e);
            }
        }

        log.info("Tier ID: {} — no criteria set matched", tierId);
        return false;
    }

    private boolean evaluateNode(JsonNode node, Map<String, Object> context) {
        if (node.has("operator") && (node.get("operator").asText().equalsIgnoreCase("AND") || node.get("operator").asText().equalsIgnoreCase("OR"))) {
            String operator = node.get("operator").asText();
            JsonNode rules = node.get("rules");
            if (rules == null || !rules.isArray()) {
                return false;
            }
            if (operator.equalsIgnoreCase("AND")) {
                for (JsonNode subRule : rules) {
                    if (!evaluateNode(subRule, context)) {
                        return false;
                    }
                }
                return true;
            } else { // OR
                for (JsonNode subRule : rules) {
                    if (evaluateNode(subRule, context)) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            return evaluateLeafRule(node, context);
        }
    }

    private boolean evaluateLeafRule(JsonNode node, Map<String, Object> context) {
        String field = node.path("field").asText();
        String operator = node.path("operator").asText();
        JsonNode valueNode = node.path("value");

        if (field.isEmpty() || operator.isEmpty()) {
            log.warn("Leaf rule lacks field or operator specification: {}", node);
            return false;
        }

        if (!context.containsKey(field)) {
            log.warn("Context parameters do not contain field: {}", field);
            return false;
        }

        Object contextValue = context.get(field);
        if (contextValue == null) {
            return valueNode.isNull();
        }

        switch (operator.toUpperCase()) {
            case "EQUALS":
                return compareEquals(contextValue, valueNode);
            case "NOT_EQUALS":
                return !compareEquals(contextValue, valueNode);
            case "GREATER_THAN":
                return compareNumeric(contextValue, valueNode) > 0;
            case "GREATER_THAN_OR_EQUAL":
                return compareNumeric(contextValue, valueNode) >= 0;
            case "LESS_THAN":
                return compareNumeric(contextValue, valueNode) < 0;
            case "LESS_THAN_OR_EQUAL":
                return compareNumeric(contextValue, valueNode) <= 0;
            default:
                log.error("Unsupported evaluation operator: {}", operator);
                return false;
        }
    }

    private boolean compareEquals(Object contextVal, JsonNode ruleValNode) {
        if (ruleValNode.isBoolean()) {
            return contextVal instanceof Boolean && ((Boolean) contextVal).equals(ruleValNode.asBoolean());
        }
        if (ruleValNode.isNumber()) {
            try {
                double contextNum = ((Number) contextVal).doubleValue();
                double ruleNum = ruleValNode.asDouble();
                return Double.compare(contextNum, ruleNum) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        return contextVal.toString().equals(ruleValNode.asText());
    }

    private int compareNumeric(Object contextVal, JsonNode ruleValNode) {
        if (!(contextVal instanceof Number)) {
            throw new IllegalArgumentException("Context value for numeric comparison is not a Number: " + contextVal);
        }
        double contextNum = ((Number) contextVal).doubleValue();
        double ruleNum = ruleValNode.asDouble();
        return Double.compare(contextNum, ruleNum);
    }
}