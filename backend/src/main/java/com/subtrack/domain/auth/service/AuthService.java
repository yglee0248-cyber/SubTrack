package com.subtrack.domain.auth.service;

import com.subtrack.domain.auth.dto.LoginRequest;
import com.subtrack.domain.auth.dto.LoginResponse;
import com.subtrack.domain.auth.dto.LogoutResponse;
import com.subtrack.domain.auth.dto.SignupRequest;
import com.subtrack.domain.auth.dto.SignupResponse;
import com.subtrack.domain.member.dao.MemberDao;
import com.subtrack.domain.member.vo.Member;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import com.subtrack.global.security.JwtTokenProvider;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ROLE_USER = "USER";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String INVALID_LOGIN_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberDao memberDao, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (memberDao.existsByEmail(email) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }

        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        member.setNickname(request.getNickname().trim());
        member.setRole(ROLE_USER);
        member.setStatus(STATUS_ACTIVE);

        try {
            memberDao.insertMember(member);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }

        return new SignupResponse(member.getMemberId(), member.getEmail(), member.getNickname());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        Member member = memberDao.findByEmail(email);

        if (member == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, INVALID_LOGIN_MESSAGE);
        }

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "로그인할 수 없는 회원입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, INVALID_LOGIN_MESSAGE);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId());
        LoginResponse.MemberSummary memberSummary = new LoginResponse.MemberSummary(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname()
        );

        return new LoginResponse(accessToken, memberSummary);
    }

    public LogoutResponse logout() {
        return new LogoutResponse(true);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
