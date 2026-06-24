package com.subtrack.domain.subscription.controller;

import com.subtrack.domain.subscription.dto.SubscriptionCreateRequest;
import com.subtrack.domain.subscription.dto.SubscriptionCreateResponse;
import com.subtrack.domain.subscription.dto.SubscriptionDeleteResponse;
import com.subtrack.domain.subscription.dto.SubscriptionDetailResponse;
import com.subtrack.domain.subscription.dto.SubscriptionListResponse;
import com.subtrack.domain.subscription.dto.SubscriptionUpdateRequest;
import com.subtrack.domain.subscription.dto.SubscriptionUpdateResponse;
import com.subtrack.domain.subscription.service.SubscriptionService;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.response.ApiResponse;
import com.subtrack.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ApiResponse<SubscriptionListResponse> getSubscriptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ApiResponse.success(subscriptionService.getSubscriptions(
                getCurrentMemberId(),
                keyword,
                categoryId,
                status,
                paymentStatus,
                page,
                size
        ));
    }

    @PostMapping
    public ApiResponse<SubscriptionCreateResponse> createSubscription(
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        return ApiResponse.success(subscriptionService.createSubscription(getCurrentMemberId(), request));
    }

    @GetMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionDetailResponse> getSubscription(
            @PathVariable Long subscriptionId
    ) {
        return ApiResponse.success(subscriptionService.getSubscription(getCurrentMemberId(), subscriptionId));
    }

    @PutMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionUpdateResponse> updateSubscription(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody SubscriptionUpdateRequest request
    ) {
        return ApiResponse.success(subscriptionService.updateSubscription(getCurrentMemberId(), subscriptionId, request));
    }

    @DeleteMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionDeleteResponse> deleteSubscription(
            @PathVariable Long subscriptionId
    ) {
        return ApiResponse.success(subscriptionService.deleteSubscription(getCurrentMemberId(), subscriptionId));
    }

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getMemberId();
    }
}
