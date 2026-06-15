package com.subtrack.domain.auth.dto;

import com.subtrack.domain.member.vo.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupResponse {

    private Long memberId;
    private String email;
    private String nickname;

    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getMemberId(), member.getEmail(), member.getNickname());
    }
}
