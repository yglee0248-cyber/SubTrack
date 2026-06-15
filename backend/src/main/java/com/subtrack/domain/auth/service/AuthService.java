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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (memberDao.countByEmail(email) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }

        Member member = Member.createForSignup(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        memberDao.insertMember(member);
        return SignupResponse.from(member);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        Member member = memberDao.findByEmail(email);

        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!isActiveMember(member)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "비활성화되었거나 탈퇴한 회원입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getMemberId(),
                member.getEmail(),
                member.getRole()
        );

        return LoginResponse.of(accessToken, member);
    }

    public LogoutResponse logout() {
        return LogoutResponse.success();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isActiveMember(Member member) {
        return ACTIVE_STATUS.equals(member.getStatus()) && member.getDeletedAt() == null;
    }
}
