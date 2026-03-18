package com.malgn.user;



import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;


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

    public void updatePassword(String password) {
        this.password = password;
        this.lastModifiedDate = LocalDateTime.now();
    }

}
