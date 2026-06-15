package com.subtrack.domain.member.service;

import com.subtrack.domain.member.dao.MemberDao;
import com.subtrack.domain.member.dto.MemberMeResponse;
import com.subtrack.domain.member.dto.NicknameUpdateRequest;
import com.subtrack.domain.member.dto.NicknameUpdateResponse;
import com.subtrack.domain.member.vo.Member;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final MemberDao memberDao;

    @Transactional(readOnly = true)
    public MemberMeResponse getMe(Long memberId) {
        Member member = getActiveMember(memberId);
        return MemberMeResponse.from(member);
    }

    @Transactional
    public NicknameUpdateResponse updateNickname(Long memberId, NicknameUpdateRequest request) {
        getActiveMember(memberId);

        int updatedCount = memberDao.updateNickname(memberId, request.getNickname());
        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다.");
        }

        Member updatedMember = getActiveMember(memberId);
        return NicknameUpdateResponse.from(updatedMember);
    }

    private Member getActiveMember(Long memberId) {
        Member member = memberDao.findById(memberId);

        if (member == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보를 확인할 수 없습니다.");
        }

        if (!ACTIVE_STATUS.equals(member.getStatus()) || member.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "비활성화되었거나 탈퇴한 회원입니다.");
        }

        return member;
    }
}
