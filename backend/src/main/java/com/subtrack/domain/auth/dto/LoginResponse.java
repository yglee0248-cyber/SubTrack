package com.subtrack.domain.auth.dto;

public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final MemberSummary member;

    public LoginResponse(String accessToken, MemberSummary member) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.member = member;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public MemberSummary getMember() {
        return member;
    }

    public static class MemberSummary {

        private final Long memberId;
        private final String email;
        private final String nickname;

        public MemberSummary(Long memberId, String email, String nickname) {
            this.memberId = memberId;
            this.email = email;
            this.nickname = nickname;
        }

        public Long getMemberId() {
            return memberId;
        }

        public String getEmail() {
            return email;
        }

        public String getNickname() {
            return nickname;
        }
    }
}
