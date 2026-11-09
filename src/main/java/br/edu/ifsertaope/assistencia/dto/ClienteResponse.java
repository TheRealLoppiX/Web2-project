package br.edu.ifsertaope.assistencia.dto;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String telefone,
        String email,
        String cpfCnpj,
        LocalDateTime dataCadastro) {
}
