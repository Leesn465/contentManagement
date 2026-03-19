package com.malgn.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 * - 인증 및 권한(Role)을 관리하는 도메인
 * - username은 유니크 제약 조건을 가지며 로그인 식별자로 사용된다.
 * - 생성/수정 시간은 validate 정책에 맞춰 DB 컬럼과 매핑된다.
 */
@Entity
@Table(name ="users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false,length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Builder
    public User(String username, String password, Role role, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * 비밀번호 변경
     * - 비밀번호 수정 시 수정 시간(lastModifiedDate)도 함께 갱신한다. (후에 확장 고려)
     */
    public void updatePassword(String password) {
        this.password = password;
        this.lastModifiedDate = LocalDateTime.now();
    }

}
