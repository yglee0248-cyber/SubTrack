package com.subtrack.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        // TODO: Implement member lookup through MemberDao when the auth/member domain is added.
        throw new UsernameNotFoundException("Member lookup is not implemented yet. memberId=" + memberId);
    }
}
