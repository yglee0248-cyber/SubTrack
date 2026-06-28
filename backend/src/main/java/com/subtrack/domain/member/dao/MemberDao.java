package com.subtrack.domain.member.dao;

import com.subtrack.domain.member.vo.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberDao {

    Member findByEmail(String email);

    Member findById(Long memberId);

    int existsByEmail(String email);

    int insertMember(Member member);

    int updateNickname(@Param("memberId") Long memberId, @Param("nickname") String nickname);

    int updatePasswordHash(@Param("memberId") Long memberId, @Param("passwordHash") String passwordHash);
}
