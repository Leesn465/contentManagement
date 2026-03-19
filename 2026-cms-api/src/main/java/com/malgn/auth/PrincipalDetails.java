package com.malgn.auth;


import com.malgn.user.Role;
import com.malgn.user.User;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 사용자 정보 객체
 * - User 엔티티를 기반으로 인증 정보 제공
 * - 권한(Role)을 GrantedAuthority 형태로 변환
 */
@Getter
@NullMarked
public class PrincipalDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Role role;

    public PrincipalDetails(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
    }
    /**
     * 사용자 권한 반환
     * - ROLE_ prefix를 붙여 Spring Security 권한 규칙에 맞춤
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return username;
    }

}
