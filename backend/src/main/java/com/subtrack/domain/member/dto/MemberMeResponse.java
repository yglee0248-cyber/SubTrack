package com.subtrack.domain.member.dto;

import java.time.LocalDateTime;

public class MemberMeResponse {

    private final Long memberId;
    private final String email;
    private final String nickname;
    private final Boolean pushEnabled;
    private final LocalDateTime createdAt;

    public MemberMeResponse(Long memberId, String email, String nickname, Boolean pushEnabled, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
        this.pushEnabled = pushEnabled;
        this.createdAt = createdAt;
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

    public Boolean getPushEnabled() {
        return pushEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
