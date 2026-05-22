package com.devinder.loyalty.service;

import java.util.Map;

public interface TierEvaluationService {
    boolean evaluateEligibility(String tierId, Map<String, Object> contextParameters);
}
