package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreatePaymentIntentRequest;
import com.devinder.loyalty.dto.request.RefundPaymentRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.PaymentIntentResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentIntentService {
    PaymentIntentResponse createPayment(CreatePaymentIntentRequest request, String defaultUsername);
    PaymentIntentResponse getPaymentById(String id);
    PageResponse<PaymentIntentResponse> getAllPayments(Pageable pageable);
    PaymentIntentResponse refundPayment(String id, RefundPaymentRequest request);
    PageResponse<PaymentIntentResponse> getMyPayments(String username, Pageable pageable);
    PaymentIntentResponse getMyPaymentById(String id, String username);
}
