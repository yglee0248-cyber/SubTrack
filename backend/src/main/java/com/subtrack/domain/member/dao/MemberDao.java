package com.subtrack.domain.member.dao;

import com.subtrack.domain.member.vo.Member;
import org.apache.ibatis.annotations.Param;

public interface MemberDao {

    int countByEmail(@Param("email") String email);

    Member findByEmail(@Param("email") String email);

    Member findById(@Param("memberId") Long memberId);

    int insertMember(Member member);

    int updateNickname(@Param("memberId") Long memberId, @Param("nickname") String nickname);
}
