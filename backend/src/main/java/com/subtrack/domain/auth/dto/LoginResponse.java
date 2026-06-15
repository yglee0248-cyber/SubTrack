package com.subtrack.domain.auth.dto;

import com.subtrack.domain.member.vo.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {

    private static final String TOKEN_TYPE = "Bearer";

    private String accessToken;
    private String tokenType;
    private MemberInfo member;

    public static LoginResponse of(String accessToken, Member member) {
        return new LoginResponse(accessToken, TOKEN_TYPE, MemberInfo.from(member));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MemberInfo {

        private Long memberId;
        private String email;
        private String nickname;

        private static MemberInfo from(Member member) {
            return new MemberInfo(member.getMemberId(), member.getEmail(), member.getNickname());
        }
    }
}
