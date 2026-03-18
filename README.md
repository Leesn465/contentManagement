# contentManagement
# 1. 프로젝트 소개

Spring Boot 기반으로 구현한 간단한 CMS(Content Management System) REST API 프로젝트입니다.

현재까지 구현한 범위는 다음과 같습니다.

- 회원가입
- 로그인
- JWT 기반 인증
- Spring Security 설정
- 비밀번호 BCrypt 암호화 저장
- 사용자 권한(Role) 관리 구조

---

# 2. 기술 스택

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA
- H2 Database
- Lombok
- JWT

---

# 3. 실행 방법

프로젝트 실행



bash
./gradlew bootRun
# 또는 IDE에서 메인 애플리케이션 실행




H2 Console

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `admin`
- Password: `0000`

# 4. 현재 구현 내용

## 4-1. 회원가입

`POST /api/auth/signup`

- `username`, `password`를 받아 사용자 생성
- 비밀번호는 `PasswordEncoder`(BCrypt)로 암호화 후 저장
- 회원가입 시 기본 권한은 `USER`

## 4-2. 로그인

`POST /api/auth/login`

- `username`, `password` 기반 로그인
- 로그인 성공 시 JWT Access Token 발급

## 4-3. 인증 방식

- JWT 기반 인증 방식 사용
- 인증이 필요한 요청은 `Authorization: Bearer {token}` 헤더 사용
- JWT 필터를 통해 토큰 검증 후 인증 정보 설정

## 4-4. 권한 구조

- `ADMIN`
- `USER`

현재 권한은 enum 기반 단일 컬럼으로 관리하도록 설계했습니다.

# 5. API 명세

## 회원가입

`POST /api/auth/signup`

**Request**



json
{
"username": "user1",
"password": "user1234"
}




**Response**



json
{
"id": 1,
"username": "user1",
"role": "USER"
}




## 로그인

`POST /api/auth/login`

**Request**



json
{
"username": "user1",
"password": "user1234"
}




**Response**



json
{
"accessToken": "eyJ...",
"tokenType": "Bearer"
}




# 6. 데이터베이스 설계

**users**

| 컬럼명             | 타입          | 제약조건       | 설명                                  |
| ------------------ | ------------- | ------------- | ------------------------------------- |
| id                 | bigint        | PK            | 사용자 ID                               |
| username           | varchar(50)   | not null, unique | 로그인 아이디                             |
| password           | varchar(255)  | not null      | 암호화된 비밀번호                           |
| role               | varchar(20)   | not null      | USER / ADMIN                            |
| created_date       | timestamp     | not null      | 생성일시                                |
| last_modified_date | timestamp     |               | 수정일시                                |

# 7. 설계 의도

## 비밀번호 암호화

사용자 비밀번호는 평문으로 저장하지 않고 `BCryptPasswordEncoder`를 사용해 암호화하여 저장했습니다.

## 인증 방식

REST API 과제 특성에 맞게 JWT 기반 인증 방식을 선택했습니다. 세션 기반 인증보다 테스트와 확장이 용이하다고 판단했습니다.

## 권한 관리

현재 과제 요구사항의 권한은 ADMIN, USER 두 가지로 명확하므로, 별도 Role 테이블 대신 enum 기반 단일 컬럼으로 단순하게 설계했습니다.

# 8. 패키지 구조

예시 기준:




com.management.content
├── auth
│ ├── controller
│ ├── dto
│ ├── jwt
│ └── service
├── user
│ ├── entity
│ ├── repository
│ └── service
├── common
└── config




# 9. 현재까지 구현한 주요 클래스

- User
- Role
- UserRepository
- AuthController
- AuthService
- CustomUserDetails
- CustomUserDetailsService
- JwtConstants
- JwtTokenProvider
- JwtAuthenticationFilter
- JwtAuthorizationFilter
- SecurityConfig
- EncoderConfig

# 10. 사용한 AI 도구 및 참고 자료

## AI 도구

- ChatGPT

## 활용 방식

- Spring Security / JWT 구조 정리
- 회원가입 / 로그인 구현 흐름 점검
- README 작성 구조 정리

최종 구현과 코드 검증은 직접 수행했습니다.
