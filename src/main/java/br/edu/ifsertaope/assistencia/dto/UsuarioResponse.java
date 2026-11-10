package br.edu.ifsertaope.assistencia.dto;

import br.edu.ifsertaope.assistencia.model.Perfil;

public record UsuarioResponse(
        Long id,
        String nome,
        String login,
        Perfil perfil,
        boolean ativo) {
}
