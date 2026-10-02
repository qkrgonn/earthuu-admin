package com.earthuu.admin.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI earthuuAdminOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Earthuu Admin API")
                .description("이벤트 심사, 신고 처리와 운영 통계를 위한 관리자 API")
                .version("v1"));
    }
}
