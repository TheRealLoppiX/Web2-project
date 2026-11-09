package br.edu.ifsertaope.assistencia.mapper;

import java.util.Locale;

public final class Textos {

    private Textos() {
    }

    public static String vazioParaNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
