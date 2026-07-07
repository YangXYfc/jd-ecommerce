package com.jd.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI jdOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("京东风格电商平台 API")
                        .description("B2C电商平台后端接口文档 - Spring Boot + MyBatis + MySQL")
                        .version("1.0.0")
                        .contact(new Contact().name("后端大师-王拜佛")));
    }
}
