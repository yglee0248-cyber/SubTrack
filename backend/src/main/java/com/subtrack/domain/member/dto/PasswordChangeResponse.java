package com.subtrack.domain.member.dto;

public class PasswordChangeResponse {

    private final boolean changed;

    public PasswordChangeResponse(boolean changed) {
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }
}
