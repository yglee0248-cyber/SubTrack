package com.subtrack.domain.auth.controller;

import com.subtrack.domain.auth.dto.LoginRequest;
import com.subtrack.domain.auth.dto.LoginResponse;
import com.subtrack.domain.auth.dto.LogoutResponse;
import com.subtrack.domain.auth.dto.SignupRequest;
import com.subtrack.domain.auth.dto.SignupResponse;
import com.subtrack.domain.auth.service.AuthService;
import com.subtrack.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout() {
        return ApiResponse.success(authService.logout());
    }
}
