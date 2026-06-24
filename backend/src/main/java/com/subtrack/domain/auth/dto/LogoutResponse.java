package com.subtrack.domain.auth.dto;

public class LogoutResponse {

    private final boolean loggedOut;

    public LogoutResponse(boolean loggedOut) {
        this.loggedOut = loggedOut;
    }

    public boolean isLoggedOut() {
        return loggedOut;
    }
}
