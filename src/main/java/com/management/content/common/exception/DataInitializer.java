package com.management.content.common.exception;

import com.management.content.content.Content;
import com.management.content.content.ContentRepository;
import com.management.content.user.Role;
import com.management.content.user.User;
import com.management.content.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            ContentRepository contentRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            User admin = userRepository.findByUsername("admin")
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .username("admin")
                                    .password(passwordEncoder.encode("admin1234"))
                                    .role(Role.ADMIN)
                                    .createdDate(LocalDateTime.now())
                                    .lastModifiedDate(LocalDateTime.now())
                                    .build()
                    ));

            if (contentRepository.count() == 0) {
                for (int i = 1; i <= 3000; i++) {
                    Content content = Content.builder()
                            .title("테스트 콘텐츠 " + i)
                            .description("테스트 내용 " + i)
                            .viewCount(0L)
                            .createdDate(LocalDateTime.now().minusMinutes(i))
                            .createdBy(admin.getUsername())
                            .lastModifiedDate(LocalDateTime.now().minusMinutes(i))
                            .lastModifiedBy(admin.getUsername())
                            .author(admin)
                            .build();

                    contentRepository.save(content);
                }
            }
        };
    }
}