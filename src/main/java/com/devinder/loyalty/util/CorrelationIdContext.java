package com.devinder.loyalty.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class CorrelationIdContext {
    public static final String MDC_KEY = "correlationId";

    public static String getCorrelationId() {
        String correlationId = MDC.get(MDC_KEY);
        return correlationId != null ? correlationId : "";
    }
}
