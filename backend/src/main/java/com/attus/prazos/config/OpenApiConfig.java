package com.attus.prazos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Monitor de Prazos Processuais API")
                        .description("API para cadastrar, editar, listar e cumprir prazos processuais.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Attus Prazos"))
                        .license(new License()
                                .name("MIT")));
    }
}
