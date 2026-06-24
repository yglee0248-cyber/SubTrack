package com.subtrack.global.security;

import com.subtrack.domain.member.dao.MemberDao;
import com.subtrack.domain.member.vo.Member;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberDao memberDao;

    public CustomUserDetailsService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        Long parsedMemberId = parseMemberId(memberId);
        Member member = memberDao.findById(parsedMemberId);

        if (member == null || !member.isActive()) {
            throw new UsernameNotFoundException("Active member not found. memberId=" + memberId);
        }

        return new CustomUserDetails(
                member.getMemberId(),
                member.getEmail(),
                member.getPasswordHash(),
                member.getRole()
        );
    }

    private Long parseMemberId(String memberId) {
        try {
            return Long.valueOf(memberId);
        } catch (NumberFormatException ex) {
            throw new UsernameNotFoundException("Invalid member id. memberId=" + memberId, ex);
        }
    }
}
