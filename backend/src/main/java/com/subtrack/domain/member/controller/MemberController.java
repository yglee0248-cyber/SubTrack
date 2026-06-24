package com.subtrack.domain.member.controller;

import com.subtrack.domain.member.dto.MemberMeResponse;
import com.subtrack.domain.member.dto.NicknameUpdateRequest;
import com.subtrack.domain.member.dto.NicknameUpdateResponse;
import com.subtrack.domain.member.service.MemberService;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.response.ApiResponse;
import com.subtrack.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/me")
    public ApiResponse<MemberMeResponse> getMe() {
        return ApiResponse.success(memberService.getMe(getCurrentMemberId()));
    }

    @PatchMapping("/me/nickname")
    public ApiResponse<NicknameUpdateResponse> updateNickname(
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        return ApiResponse.success(memberService.updateNickname(getCurrentMemberId(), request));
    }

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getMemberId();
    }
}
