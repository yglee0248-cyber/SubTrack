package com.subtrack.domain.member.service;

import com.subtrack.domain.member.dao.MemberDao;
import com.subtrack.domain.member.dto.MemberMeResponse;
import com.subtrack.domain.member.dto.NicknameUpdateRequest;
import com.subtrack.domain.member.dto.NicknameUpdateResponse;
import com.subtrack.domain.member.dto.PasswordChangeRequest;
import com.subtrack.domain.member.dto.PasswordChangeResponse;
import com.subtrack.domain.member.dto.ProfileUpdateRequest;
import com.subtrack.domain.member.vo.Member;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberDao memberDao, PasswordEncoder passwordEncoder) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public MemberMeResponse getMe(Long memberId) {
        Member member = findActiveMember(memberId);

        return new MemberMeResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getPushEnabled(),
                member.getCreatedAt()
        );
    }

    @Transactional
    public NicknameUpdateResponse updateNickname(Long memberId, NicknameUpdateRequest request) {
        MemberMeResponse profile = updateProfile(memberId, toProfileUpdateRequest(request.getNickname()));

        return new NicknameUpdateResponse(profile.getMemberId(), profile.getNickname());
    }

    @Transactional
    public MemberMeResponse updateProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = findActiveMember(memberId);
        String nickname = request.getNickname().trim();

        int updatedCount = memberDao.updateNickname(member.getMemberId(), nickname);
        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "수정할 수 없는 회원입니다.");
        }

        return new MemberMeResponse(
                member.getMemberId(),
                member.getEmail(),
                nickname,
                member.getPushEnabled(),
                member.getCreatedAt()
        );
    }

    @Transactional
    public PasswordChangeResponse changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = findActiveMember(memberId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCHED);
        }

        if (passwordEncoder.matches(request.getNewPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        int updatedCount = memberDao.updatePasswordHash(
                member.getMemberId(),
                passwordEncoder.encode(request.getNewPassword())
        );
        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "비밀번호를 변경할 수 없는 회원입니다.");
        }

        return new PasswordChangeResponse(true);
    }

    private Member findActiveMember(Long memberId) {
        Member member = memberDao.findById(memberId);

        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이용할 수 없는 회원입니다.");
        }

        return member;
    }

    private ProfileUpdateRequest toProfileUpdateRequest(String nickname) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setNickname(nickname);
        return request;
    }
}
