package br.edu.ifsertaope.assistencia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiDePedidos() {
        return new OpenAPI().info(new Info()
                .title("API de Pedidos - Assistência Técnica")
                .version("1.0.0")
                .description("API REST para gerenciamento de clientes, equipamentos, técnicos e ordens de serviço "
                        + "de uma assistência técnica de informática.")
                .contact(new Contact().name("Arthur Vasconcelos de Oliveira e Bruno Alencar - IFSertão-PE")));
    }
}
