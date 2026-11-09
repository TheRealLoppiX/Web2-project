package br.edu.ifsertaope.assistencia.model;

import static br.edu.ifsertaope.assistencia.model.StatusOrdemServico.AGUARDANDO_ANALISE;
import static br.edu.ifsertaope.assistencia.model.StatusOrdemServico.CANCELADO;
import static br.edu.ifsertaope.assistencia.model.StatusOrdemServico.CONCLUIDO;
import static br.edu.ifsertaope.assistencia.model.StatusOrdemServico.EM_REPARO;
import static br.edu.ifsertaope.assistencia.model.StatusOrdemServico.ENTREGUE;
import static br.edu.ifsertaope.assistencia.model.StatusOrdemServico.ORCAMENTO_APROVADO;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class StatusOrdemServicoTest {

    static Stream<Arguments> transicoesDoDiagrama() {
        return Stream.of(
                Arguments.of(AGUARDANDO_ANALISE, EnumSet.of(ORCAMENTO_APROVADO, CANCELADO)),
                Arguments.of(ORCAMENTO_APROVADO, EnumSet.of(EM_REPARO, CANCELADO)),
                Arguments.of(EM_REPARO, EnumSet.of(CONCLUIDO)),
                Arguments.of(CONCLUIDO, EnumSet.of(ENTREGUE)),
                Arguments.of(ENTREGUE, EnumSet.noneOf(StatusOrdemServico.class)),
                Arguments.of(CANCELADO, EnumSet.noneOf(StatusOrdemServico.class)));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("transicoesDoDiagrama")
    @DisplayName("Cada status permite exatamente as transições do diagrama de estados")
    void permiteSomenteTransicoesDoDiagrama(StatusOrdemServico origem, Set<StatusOrdemServico> permitidos) {
        for (StatusOrdemServico destino : StatusOrdemServico.values()) {
            assertThat(origem.podeTransicionarPara(destino))
                    .as("%s -> %s", origem, destino)
                    .isEqualTo(permitidos.contains(destino));
        }
    }

    @Test
    void naoPermiteTransicaoParaNulo() {
        assertThat(AGUARDANDO_ANALISE.podeTransicionarPara(null)).isFalse();
    }

    @Test
    void somenteEntregueECanceladoSaoFinais() {
        assertThat(EnumSet.allOf(StatusOrdemServico.class).stream().filter(StatusOrdemServico::isFinal))
                .containsExactlyInAnyOrder(ENTREGUE, CANCELADO);
    }

    @Test
    void cancelamentoSoAntesDoReparo() {
        assertThat(EnumSet.allOf(StatusOrdemServico.class).stream().filter(s -> s.podeTransicionarPara(CANCELADO)))
                .containsExactlyInAnyOrder(AGUARDANDO_ANALISE, ORCAMENTO_APROVADO);
    }

    @Test
    void entrarEmConcluidoPreencheDataDeConclusao() {
        OrdemServico os = new OrdemServico();
        LocalDateTime momento = LocalDateTime.of(2026, 11, 9, 14, 30);

        CONCLUIDO.aoEntrar(os, momento);

        assertThat(os.getDataConclusao()).isEqualTo(momento);
        assertThat(os.getDataEntrega()).isNull();
    }

    @Test
    void entrarEmEntreguePreencheDataDeEntrega() {
        OrdemServico os = new OrdemServico();
        LocalDateTime momento = LocalDateTime.of(2026, 11, 10, 9, 0);

        ENTREGUE.aoEntrar(os, momento);

        assertThat(os.getDataEntrega()).isEqualTo(momento);
        assertThat(os.getDataConclusao()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = {"AGUARDANDO_ANALISE", "ORCAMENTO_APROVADO", "EM_REPARO",
            "CANCELADO"})
    void demaisStatusNaoAlteramDatas(StatusOrdemServico status) {
        OrdemServico os = new OrdemServico();

        status.aoEntrar(os, LocalDateTime.now());

        assertThat(os.getDataConclusao()).isNull();
        assertThat(os.getDataEntrega()).isNull();
    }
}
