package br.edu.ifsertaope.assistencia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI apiDePedidos() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Pedidos - Assistência Técnica")
                        .version("1.1.0")
                        .description("API REST para gerenciamento de clientes, equipamentos, técnicos e ordens de "
                                + "serviço de uma assistência técnica de informática. Faça login em "
                                + "POST /api/auth/login e use o token no botão Authorize.")
                        .contact(new Contact().name("Arthur Vasconcelos de Oliveira e Bruno Alencar - IFSertão-PE")))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
