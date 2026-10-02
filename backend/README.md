# Backend

Java 21과 Spring Boot 4 기반 Earthuu 관리자 API입니다.

## 현재 구현

- 기능별 패키지 구조
- PostgreSQL + JPA + Flyway
- Spring Security 서버 세션 인증
- CSRF 쿠키/헤더 보호
- 공통 API 응답 및 예외 처리
- Swagger UI/OpenAPI
- 관리자 계정 초기화 옵션

## 로컬 실행

PostgreSQL 데이터베이스를 준비한 뒤 환경 변수를 설정합니다.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/earthuu
export DB_USERNAME=earthuu
export DB_PASSWORD=change-me
export BOOTSTRAP_ADMIN_ENABLED=true
export BOOTSTRAP_ADMIN_EMAIL=admin@earthuu.local
export BOOTSTRAP_ADMIN_PASSWORD=change-me-now
./mvnw spring-boot:run
```

`application-local.example.yml`을 복사해 Git에서 제외되는 `application-local.yml`로 사용해도 됩니다.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 확인 주소

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Health: <http://localhost:8080/actuator/health>

## 테스트

테스트는 PostgreSQL 호환 모드의 인메모리 H2를 사용합니다.

```bash
./mvnw test
```

## 세션 로그인 순서

1. `GET /api/admin/v1/auth/csrf`로 CSRF 토큰을 받습니다.
2. 응답의 토큰을 `X-XSRF-TOKEN` 헤더로 전달하며 `POST /api/admin/v1/auth/login`을 호출합니다.
3. 이후 요청은 발급된 `JSESSIONID` 쿠키와 CSRF 헤더를 함께 전달합니다.

Java 21과 Spring Boot로 구현할 관리자 API 영역입니다.

## 예정 구성

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL Driver
- Flyway
- Springdoc OpenAPI
- JUnit 5 / MockMvc

## 패키지 역할

- `config`: 보안, CORS, OpenAPI 등 공통 설정
- `controller`: `/api/admin/**` HTTP 엔드포인트
- `dto`: 요청 및 응답 모델
- `entity`: ERD에 대응하는 JPA 엔티티
- `repository`: 데이터 조회 및 저장
- `service`: 심사, 신고 처리, 통계 업무 규칙
- `exception`: 공통 오류 응답과 예외 처리
- `resources/db/migration`: Flyway SQL 마이그레이션
- `test`: 서비스 및 API 자동화 테스트

Spring Boot 프로젝트를 초기화할 때 group은 `com.earthuu`, artifact는 `admin`, package는 `com.earthuu.admin`, Java 버전은 `21`로 설정합니다.

Swagger UI와 OpenAPI JSON의 기본 제공 경로는 다음과 같이 구성합니다.

```text
/swagger-ui/index.html
/v3/api-docs
```
