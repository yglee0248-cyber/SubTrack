package com.subtrack.global.security;

import com.subtrack.domain.member.dao.MemberDao;
import com.subtrack.domain.member.vo.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String ROLE_PREFIX = "ROLE_";

    private final MemberDao memberDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberDao.findByEmail(username);
        return createUserDetails(member);
    }

    public UserDetails loadUserByMemberId(Long memberId) {
        Member member = memberDao.findById(memberId);
        return createUserDetails(member);
    }

    private UserDetails createUserDetails(Member member) {
        if (member == null || !isActiveMember(member)) {
            throw new UsernameNotFoundException("인증 가능한 회원 정보를 찾을 수 없습니다.");
        }

        return new CustomUserDetails(
                member.getMemberId(),
                member.getEmail(),
                member.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(ROLE_PREFIX + member.getRole()))
        );
    }

    private boolean isActiveMember(Member member) {
        return ACTIVE_STATUS.equals(member.getStatus()) && member.getDeletedAt() == null;
    }
}
