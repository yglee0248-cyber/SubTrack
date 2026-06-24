package com.subtrack.domain.member.service;

import com.subtrack.domain.member.dao.MemberDao;
import com.subtrack.domain.member.dto.MemberMeResponse;
import com.subtrack.domain.member.dto.NicknameUpdateRequest;
import com.subtrack.domain.member.dto.NicknameUpdateResponse;
import com.subtrack.domain.member.vo.Member;
import com.subtrack.global.exception.BusinessException;
import com.subtrack.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
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
        Member member = findActiveMember(memberId);
        String nickname = request.getNickname().trim();

        int updatedCount = memberDao.updateNickname(member.getMemberId(), nickname);
        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "수정할 수 없는 회원입니다.");
        }

        return new NicknameUpdateResponse(member.getMemberId(), nickname);
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
}
