package com.subtrack.domain.member.vo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    private static final String DEFAULT_ROLE = "USER";
    private static final String DEFAULT_STATUS = "ACTIVE";

    private Long memberId;
    private String email;
    private String passwordHash;
    private String nickname;
    private String role;
    private String status;
    private Boolean pushEnabled;
    private String onesignalPlayerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Member createForSignup(String email, String passwordHash, String nickname) {
        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordHash);
        member.setNickname(nickname);
        member.setRole(DEFAULT_ROLE);
        member.setStatus(DEFAULT_STATUS);
        return member;
    }
}
