package com.devinder.loyalty.controller;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> data = new HashMap<>();
        data.put("message", MessageConstants.SERVER_WORKING);
        
        ApiResponse<Map<String, String>> response = ApiResponse.success(data, MessageConstants.SERVER_WORKING);
        return ResponseEntity.ok(response);
    }
}
