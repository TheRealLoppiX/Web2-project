package br.edu.ifsertaope.assistencia.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seguranca")
public record SegurancaProperties(Jwt jwt, Administrador administrador) {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    public SegurancaProperties {
        if (jwt == null || jwt.segredo() == null
                || jwt.segredo().getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "Defina a variável de ambiente JWT_SECRET com pelo menos 32 caracteres.");
        }
    }

    public record Jwt(String segredo, Duration expiracao) {
    }

    public record Administrador(String nome, String login, String senha) {
    }
}
