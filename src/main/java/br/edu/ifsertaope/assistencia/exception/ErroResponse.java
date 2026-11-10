package br.edu.ifsertaope.assistencia.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos) {

    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ErroResponse de(HttpStatus status, String mensagem, String caminho, List<CampoInvalido> campos,
            LocalDateTime momento) {
        return new ErroResponse(momento, status.value(), descrever(status), mensagem, caminho, campos);
    }

    private static String descrever(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case UNAUTHORIZED -> "Não autenticado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Recurso não encontrado";
            case CONFLICT -> "Conflito";
            case UNPROCESSABLE_ENTITY -> "Regra de negócio violada";
            default -> "Erro interno";
        };
    }
}
