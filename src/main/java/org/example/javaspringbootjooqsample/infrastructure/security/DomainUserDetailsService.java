package org.example.javaspringbootjooqsample.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.service.UserLookupService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {

    private final UserLookupService userLookupService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userLookupService.requireByUsername(username);
            List<SimpleGrantedAuthority> authorities = user.getRoles() == null || user.getRoles().isEmpty()
                    ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    : user.getRoles().stream()
                    .map(role -> role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName())
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    authorities
            );
        } catch (BusinessException exception) {
            throw new UsernameNotFoundException("유효하지 않은 회원입니다.", exception);
        }
    }
}
