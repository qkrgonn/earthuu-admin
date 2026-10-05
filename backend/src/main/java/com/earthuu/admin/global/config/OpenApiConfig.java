package com.earthuu.admin.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI earthuuAdminOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("sessionAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("관리자 로그인 후 발급되는 서버 세션 쿠키"))
                        .addSecuritySchemes("csrfToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")
                                .description("GET /api/admin/v1/auth/csrf에서 발급받은 CSRF 토큰")))
                .info(new Info()
                        .title("Earthuu Admin API")
                        .description("이벤트 심사, 신고 처리와 운영 통계를 위한 관리자 API\n\n"
                                + "Swagger 사용 순서: 1) CSRF 토큰 발급 2) 관리자 로그인 3) 관리자 API 호출")
                        .version("v1"));
    }
}
