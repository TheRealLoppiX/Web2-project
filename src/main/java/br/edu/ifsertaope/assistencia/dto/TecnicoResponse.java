package br.edu.ifsertaope.assistencia.dto;

public record TecnicoResponse(
        Long id,
        String nome,
        String email,
        String especialidade,
        boolean ativo) {
}
