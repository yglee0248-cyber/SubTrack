package com.subtrack.domain.auth.dto;

public class SignupResponse {

    private final Long memberId;
    private final String email;
    private final String nickname;

    public SignupResponse(Long memberId, String email, String nickname) {
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
