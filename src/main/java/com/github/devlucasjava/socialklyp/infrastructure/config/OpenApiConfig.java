package com.github.devlucasjava.socialklyp.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide the JWT token. Example: Bearer {token}"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Social Klyp API")
                        .version("1.0.0")
                        .description("""
                                REST API for the Social Klyp social network.
                                
                                **Authentication:** All protected endpoints require a Bearer JWT token.
                                Use `/auth/login` or `/auth/register` to obtain a token, then click **Authorize** above.
                                
                                **Profile privacy:** Posts and profiles marked as private are only visible to followers.
                                
                                **Chat limit:** Each chat supports a maximum of 50 members.
                                """)
                        .contact(new Contact()
                                .name("Social Klyp Team")
                                .url("https://github.com/devlucas-java/social-klyp"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8888").description("Local development"),
                        new Server().url("https://api.socialklyp.com").description("Production")
                ));
    }
}