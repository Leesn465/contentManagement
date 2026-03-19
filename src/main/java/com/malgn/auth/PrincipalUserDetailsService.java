package com.malgn.auth;


import com.malgn.user.User;
import com.malgn.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * 사용자 인증 정보 로드 서비스
 * - username을 기반으로 사용자 조회
 * - 조회된 User를 PrincipalDetails로 변환
 */
@Service
@NullMarked
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * username으로 사용자 조회
     * - 존재하지 않을 경우 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername( String username) throws UsernameNotFoundException{
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        return new PrincipalDetails(user);
    }
}
