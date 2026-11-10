package br.edu.ifsertaope.assistencia.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    public static final String MENSAGEM_NAO_AUTENTICADO =
            "Autenticação necessária. Faça login em /api/auth/login e envie o token no cabeçalho Authorization.";
    public static final String MENSAGEM_TOKEN_INVALIDO = "Token inválido ou expirado. Faça login novamente.";
    public static final String MENSAGEM_ACESSO_NEGADO = "Seu perfil não tem permissão para esta operação.";

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RespostaErroSeguranca(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException excecao) throws IOException {
        String mensagem = excecao instanceof InvalidBearerTokenException
                ? MENSAGEM_TOKEN_INVALIDO
                : MENSAGEM_NAO_AUTENTICADO;
        response.setHeader("WWW-Authenticate", "Bearer");
        escrever(response, HttpStatus.UNAUTHORIZED, mensagem, request.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException excecao) throws IOException {
        escrever(response, HttpStatus.FORBIDDEN, MENSAGEM_ACESSO_NEGADO, request.getRequestURI());
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String mensagem, String caminho)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(),
                ErroResponse.de(status, mensagem, caminho, null, LocalDateTime.now(clock)));
    }
}
