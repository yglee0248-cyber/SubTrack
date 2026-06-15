package com.subtrack.domain.member.dto;

import com.subtrack.domain.member.vo.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NicknameUpdateResponse {

    private Long memberId;
    private String nickname;

    public static NicknameUpdateResponse from(Member member) {
        return new NicknameUpdateResponse(member.getMemberId(), member.getNickname());
    }
}
