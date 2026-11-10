package br.edu.ifsertaope.assistencia.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class RelogioAssistencia {

    public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Recife");

    private final Clock clock;

    private RelogioAssistencia() {
        this.clock = Clock.tickSeconds(FUSO_HORARIO);
    }

    public static RelogioAssistencia getInstancia() {
        return Instancia.UNICA;
    }

    public Clock clock() {
        return clock;
    }

    public LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    private static final class Instancia {
        private static final RelogioAssistencia UNICA = new RelogioAssistencia();
    }
}
