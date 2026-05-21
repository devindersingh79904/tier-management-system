package com.devinder.loyalty.config;

import com.devinder.loyalty.constants.HeaderConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loyaltyTierOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loyalty Tier System API")
                        .description("Production-grade Spring Boot API for Subscription Plans, Loyalty Tiers, and Benefit management.")
                        .version("1.0.0"));
    }

    /**
     * Add X-Correlation-Id header to OpenAPI/Swagger UI endpoints automatically.
     */
    @Bean
    public OperationCustomizer addCorrelationIdHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() == null) {
                operation.setParameters(new ArrayList<>());
            }
            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name(HeaderConstants.CORRELATION_ID)
                    .description("Correlation ID for tracking the request flow")
                    .required(false)
                    .schema(new io.swagger.v3.oas.models.media.StringSchema()));
            return operation;
        };
    }
}
