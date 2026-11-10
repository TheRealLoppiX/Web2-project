package br.edu.ifsertaope.assistencia.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class RelogioAssistenciaTest {

    @Test
    void sempreRetornaAMesmaInstancia() {
        assertThat(RelogioAssistencia.getInstancia()).isSameAs(RelogioAssistencia.getInstancia());
    }

    @Test
    void mesmaInstanciaEntreThreadsConcorrentes() throws InterruptedException {
        Set<RelogioAssistencia> instancias = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> instancias.add(RelogioAssistencia.getInstancia()));
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(instancias).hasSize(1);
    }

    @Test
    void construtorNaoEhAcessivel() {
        assertThat(RelogioAssistencia.class.getDeclaredConstructors())
                .allMatch(construtor -> Modifier.isPrivate(construtor.getModifiers()));
    }

    @Test
    void usaFusoDeRecifeEmSegundosInteiros() {
        RelogioAssistencia relogio = RelogioAssistencia.getInstancia();

        assertThat(relogio.clock().getZone()).isEqualTo(ZoneId.of("America/Recife"));
        assertThat(relogio.agora().getNano()).isZero();
    }

    @Test
    void beanDoSpringReutilizaOSingleton() {
        assertThat(new ClockConfig().clock()).isSameAs(RelogioAssistencia.getInstancia().clock());
    }
}
