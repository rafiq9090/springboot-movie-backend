package org.example.movices.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("movice api")
                        .description("movice api")
                        .version("1.0")
                );
    }
}
