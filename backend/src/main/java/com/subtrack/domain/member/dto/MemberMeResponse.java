package com.subtrack.domain.member.dto;

import com.subtrack.domain.member.vo.Member;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMeResponse {

    private Long memberId;
    private String email;
    private String nickname;
    private boolean pushEnabled;
    private LocalDateTime createdAt;

    public static MemberMeResponse from(Member member) {
        return new MemberMeResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                Boolean.TRUE.equals(member.getPushEnabled()),
                member.getCreatedAt()
        );
    }
}
