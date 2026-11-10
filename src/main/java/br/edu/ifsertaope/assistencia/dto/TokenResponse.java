package br.edu.ifsertaope.assistencia.dto;

import java.time.LocalDateTime;

import br.edu.ifsertaope.assistencia.model.Perfil;

public record TokenResponse(
        String token,
        String tipo,
        LocalDateTime expiraEm,
        String login,
        Perfil perfil) {
}
