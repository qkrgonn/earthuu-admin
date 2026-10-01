# Backend

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

