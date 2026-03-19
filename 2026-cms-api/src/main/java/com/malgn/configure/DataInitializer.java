//package com.malgn.configure;
//
//
//import com.malgn.content.Content;
//import com.malgn.content.ContentRepository;
//import com.malgn.user.Role;
//import com.malgn.user.User;
//import com.malgn.user.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//
///**
// * 초기 데이터 설정
// * - 애플리케이션 실행 시 관리자 계정 및 테스트 데이터 생성
// * - 대량 데이터(3000건)를 생성하여 페이징 및 성능 테스트 지원
// */
//@Configuration
//public class DataInitializer {
//
//    /**
//     * 테스트 데이터 초기화
//     * - admin 계정이 없으면 생성
//     * - 콘텐츠 데이터가 없을 경우 대량 데이터 생성
//     */
//    @Bean
//    CommandLineRunner initData(
//            UserRepository userRepository,
//            ContentRepository contentRepository,
//            PasswordEncoder passwordEncoder
//    ) {
//        return args -> {
//            User admin = userRepository.findByUsername("admin")
//                    .orElseGet(() -> userRepository.save(
//                            User.builder()
//                                    .username("admin")
//                                    .password(passwordEncoder.encode("admin1234"))
//                                    .role(Role.ADMIN)
//                                    .createdDate(LocalDateTime.now())
//                                    .lastModifiedDate(LocalDateTime.now())
//                                    .build()
//                    ));
//
//            if (contentRepository.count() == 0) {
//                for (int i = 1; i <= 3000; i++) {
//                    Content content = Content.builder()
//                            .title("테스트 콘텐츠 " + i)
//                            .description("테스트 내용 " + i)
//                            .viewCount(0L)
//                            .createdDate(LocalDateTime.now().minusMinutes(i))
//                            .createdBy(admin.getUsername())
//                            .lastModifiedDate(LocalDateTime.now().minusMinutes(i))
//                            .lastModifiedBy(admin.getUsername())
//                            .author(admin)
//                            .build();
//
//                    contentRepository.save(content);
//                }
//            }
//        };
//    }
//}