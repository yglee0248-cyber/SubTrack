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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<MemberMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(memberService.getMe(getMemberId(userDetails)));
    }

    @PatchMapping("/me/nickname")
    public ApiResponse<NicknameUpdateResponse> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        return ApiResponse.success(memberService.updateNickname(getMemberId(userDetails), request));
    }

    private Long getMemberId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getMemberId();
    }
}
