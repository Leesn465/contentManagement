package com.malgn.user;


import com.malgn.auth.DTO.SignUpRequest;
import com.malgn.auth.DTO.SignupResponse;
import com.malgn.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public SignupResponse signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("이미 존재하는 username입니다.");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        User saveComplete = userRepository.save(user);
        return new SignupResponse(
                saveComplete.getId(),
                saveComplete.getUsername(),
                saveComplete.getRole().name()
        );
    }


}
