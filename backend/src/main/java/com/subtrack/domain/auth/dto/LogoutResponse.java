package com.subtrack.domain.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogoutResponse {

    private boolean loggedOut;

    public static LogoutResponse success() {
        return new LogoutResponse(true);
    }
}
