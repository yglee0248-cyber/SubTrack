package com.subtrack.domain.member.dto;

public class NicknameUpdateResponse {

    private final Long memberId;
    private final String nickname;

    public NicknameUpdateResponse(Long memberId, String nickname) {
        this.memberId = memberId;
        this.nickname = nickname;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getNickname() {
        return nickname;
    }
}
