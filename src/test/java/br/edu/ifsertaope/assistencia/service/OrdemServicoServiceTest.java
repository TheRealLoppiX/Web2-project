package br.edu.ifsertaope.assistencia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifsertaope.assistencia.dto.AlteracaoStatusRequest;
import br.edu.ifsertaope.assistencia.dto.AtribuicaoTecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoAberturaRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoResponse;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.exception.RegraNegocioException;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.model.Equipamento;
import br.edu.ifsertaope.assistencia.model.HistoricoStatus;
import br.edu.ifsertaope.assistencia.model.OrdemServico;
import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;
import br.edu.ifsertaope.assistencia.model.Tecnico;
import br.edu.ifsertaope.assistencia.model.TipoEquipamento;
import br.edu.ifsertaope.assistencia.repository.HistoricoStatusRepository;
import br.edu.ifsertaope.assistencia.repository.OrdemServicoRepository;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    private static final ZoneId RECIFE = ZoneId.of("America/Recife");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-09T17:30:00Z"), RECIFE);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 9, 14, 30);

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private HistoricoStatusRepository historicoStatusRepository;

    @Mock
    private EquipamentoService equipamentoService;

    @Mock
    private TecnicoService tecnicoService;

    private OrdemServicoService service;

    private Equipamento equipamento;

    @BeforeEach
    void configurar() {
        service = new OrdemServicoService(ordemServicoRepository, historicoStatusRepository, equipamentoService,
                tecnicoService, CLOCK);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria da Silva");

        equipamento = new Equipamento();
        equipamento.setId(10L);
        equipamento.setCliente(cliente);
        equipamento.setTipo(TipoEquipamento.NOTEBOOK);
        equipamento.setMarca("Dell");
        equipamento.setModelo("Inspiron");
    }

    private OrdemServico ordemEm(StatusOrdemServico status) {
        OrdemServico os = new OrdemServico();
        os.setId(100L);
        os.setEquipamento(equipamento);
        os.setStatus(status);
        os.setDescricaoDefeito("Não liga");
        os.setDataAbertura(AGORA.minusDays(1));
        when(ordemServicoRepository.findById(100L)).thenReturn(Optional.of(os));
        return os;
    }

    private Tecnico tecnico(boolean ativo) {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(5L);
        tecnico.setNome("João");
        tecnico.setAtivo(ativo);
        return tecnico;
    }

    private HistoricoStatus historicoSalvo() {
        ArgumentCaptor<HistoricoStatus> captor = ArgumentCaptor.forClass(HistoricoStatus.class);
        verify(historicoStatusRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    class Abertura {

        @Test
        void rn01AbreEmAguardandoAnaliseERegistraPrimeiroHistorico() {
            when(equipamentoService.obter(10L)).thenReturn(equipamento);
            when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(inv -> {
                OrdemServico os = inv.getArgument(0);
                os.setId(100L);
                return os;
            });

            OrdemServicoResponse resposta = service.abrir(new OrdemServicoAberturaRequest(10L, " Não liga "));

            assertThat(resposta.id()).isEqualTo(100L);
            assertThat(resposta.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_ANALISE);
            assertThat(resposta.dataAbertura()).isEqualTo(AGORA);
            assertThat(resposta.descricaoDefeito()).isEqualTo("Não liga");
            assertThat(resposta.clienteId()).isEqualTo(1L);

            HistoricoStatus historico = historicoSalvo();
            assertThat(historico.getStatusAnterior()).isNull();
            assertThat(historico.getStatusNovo()).isEqualTo(StatusOrdemServico.AGUARDANDO_ANALISE);
            assertThat(historico.getDataHora()).isEqualTo(AGORA);
        }

        @Test
        void equipamentoInexistenteRetornaNaoEncontrado() {
            when(equipamentoService.obter(99L))
                    .thenThrow(new RecursoNaoEncontradoException("Equipamento com id 99 não encontrado."));

            assertThatThrownBy(() -> service.abrir(new OrdemServicoAberturaRequest(99L, "Não liga")))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            verify(ordemServicoRepository, never()).save(any());
        }
    }

    @Nested
    class AlteracaoDeStatus {

        @Test
        void rn02TransicaoForaDoDiagramaEhRejeitada() {
            ordemEm(StatusOrdemServico.EM_REPARO);

            assertThatThrownBy(() -> service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.ENTREGUE, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("Transição de EM_REPARO para ENTREGUE não permitida.");
            verify(historicoStatusRepository, never()).save(any());
        }

        @Test
        void rn03AprovarOrcamentoExigeLaudoEValor() {
            OrdemServico os = ordemEm(StatusOrdemServico.AGUARDANDO_ANALISE);
            os.setLaudoTecnico("Cooler obstruído");

            assertThatThrownBy(() -> service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.ORCAMENTO_APROVADO, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("laudo técnico e valor total");
            assertThat(os.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_ANALISE);
        }

        @Test
        void rn03AprovaOrcamentoComLaudoEValorERegistraHistorico() {
            OrdemServico os = ordemEm(StatusOrdemServico.AGUARDANDO_ANALISE);
            os.setLaudoTecnico("Cooler obstruído");
            os.setValorTotal(new BigDecimal("150.00"));

            OrdemServicoResponse resposta = service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.ORCAMENTO_APROVADO, "Aprovado por telefone"));

            assertThat(resposta.status()).isEqualTo(StatusOrdemServico.ORCAMENTO_APROVADO);
            HistoricoStatus historico = historicoSalvo();
            assertThat(historico.getStatusAnterior()).isEqualTo(StatusOrdemServico.AGUARDANDO_ANALISE);
            assertThat(historico.getStatusNovo()).isEqualTo(StatusOrdemServico.ORCAMENTO_APROVADO);
            assertThat(historico.getObservacao()).isEqualTo("Aprovado por telefone");
            assertThat(historico.getDataHora()).isEqualTo(AGORA);
        }

        @Test
        void rn04IniciarReparoExigeTecnicoAtribuido() {
            ordemEm(StatusOrdemServico.ORCAMENTO_APROVADO);

            assertThatThrownBy(() -> service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.EM_REPARO, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("técnico ativo atribuído");
        }

        @Test
        void rn04IniciarReparoExigeTecnicoAtivo() {
            OrdemServico os = ordemEm(StatusOrdemServico.ORCAMENTO_APROVADO);
            os.setTecnico(tecnico(false));

            assertThatThrownBy(() -> service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.EM_REPARO, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("técnico ativo atribuído");
        }

        @Test
        void rn04IniciaReparoComTecnicoAtivo() {
            OrdemServico os = ordemEm(StatusOrdemServico.ORCAMENTO_APROVADO);
            os.setTecnico(tecnico(true));

            OrdemServicoResponse resposta = service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.EM_REPARO, null));

            assertThat(resposta.status()).isEqualTo(StatusOrdemServico.EM_REPARO);
            assertThat(resposta.tecnicoId()).isEqualTo(5L);
        }

        @Test
        void rn05ConcluirExigeDescricaoDosServicos() {
            ordemEm(StatusOrdemServico.EM_REPARO);

            assertThatThrownBy(() -> service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.CONCLUIDO, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("descrição dos serviços");
        }

        @Test
        void rf15ConcluirPreencheDataDeConclusao() {
            OrdemServico os = ordemEm(StatusOrdemServico.EM_REPARO);
            os.setDescricaoServicos("Limpeza e troca de pasta térmica");

            OrdemServicoResponse resposta = service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.CONCLUIDO, null));

            assertThat(resposta.status()).isEqualTo(StatusOrdemServico.CONCLUIDO);
            assertThat(resposta.dataConclusao()).isEqualTo(AGORA);
            assertThat(resposta.dataEntrega()).isNull();
        }

        @Test
        void rf15EntregarPreencheDataDeEntrega() {
            ordemEm(StatusOrdemServico.CONCLUIDO);

            OrdemServicoResponse resposta = service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.ENTREGUE, "Retirado pelo cliente"));

            assertThat(resposta.status()).isEqualTo(StatusOrdemServico.ENTREGUE);
            assertThat(resposta.dataEntrega()).isEqualTo(AGORA);
        }

        @Test
        void rf16CancelaOsQueAindaNaoEntrouEmReparo() {
            ordemEm(StatusOrdemServico.ORCAMENTO_APROVADO);

            OrdemServicoResponse resposta = service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.CANCELADO, "Cliente desistiu"));

            assertThat(resposta.status()).isEqualTo(StatusOrdemServico.CANCELADO);
        }

        @ParameterizedTest
        @EnumSource(value = StatusOrdemServico.class, names = {"ENTREGUE", "CANCELADO"})
        void rn08OsEncerradaNaoMudaDeStatus(StatusOrdemServico encerrado) {
            ordemEm(encerrado);

            assertThatThrownBy(() -> service.alterarStatus(100L,
                    new AlteracaoStatusRequest(StatusOrdemServico.AGUARDANDO_ANALISE, null)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("Ordem de serviço com status " + encerrado + " não aceita mais alterações.");
        }

        @Test
        void osInexistenteRetornaNaoEncontrado() {
            when(ordemServicoRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.alterarStatus(404L,
                    new AlteracaoStatusRequest(StatusOrdemServico.CANCELADO, null)))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Ordem de serviço com id 404 não encontrada.");
        }
    }

    @Nested
    class AtualizacaoEAtribuicao {

        @Test
        void rf12RegistraLaudoServicosEValor() {
            ordemEm(StatusOrdemServico.AGUARDANDO_ANALISE);

            OrdemServicoResponse resposta = service.atualizar(100L, new OrdemServicoAtualizacaoRequest(
                    "Cooler obstruído", "Limpeza", new BigDecimal("150.00")));

            assertThat(resposta.laudoTecnico()).isEqualTo("Cooler obstruído");
            assertThat(resposta.descricaoServicos()).isEqualTo("Limpeza");
            assertThat(resposta.valorTotal()).isEqualByComparingTo("150.00");
        }

        @Test
        void camposNulosMantemValoresAtuais() {
            OrdemServico os = ordemEm(StatusOrdemServico.ORCAMENTO_APROVADO);
            os.setLaudoTecnico("Laudo original");
            os.setValorTotal(new BigDecimal("80.00"));

            service.atualizar(100L, new OrdemServicoAtualizacaoRequest(null, "Troca de fonte", null));

            assertThat(os.getLaudoTecnico()).isEqualTo("Laudo original");
            assertThat(os.getValorTotal()).isEqualByComparingTo("80.00");
            assertThat(os.getDescricaoServicos()).isEqualTo("Troca de fonte");
        }

        @Test
        void rn10ValorTotalNaoPodeSerNegativo() {
            ordemEm(StatusOrdemServico.AGUARDANDO_ANALISE);

            assertThatThrownBy(() -> service.atualizar(100L,
                    new OrdemServicoAtualizacaoRequest("Laudo", null, new BigDecimal("-1.00"))))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("O valor total da OS não pode ser negativo.");
        }

        @Test
        void rn08OsEntregueNaoAceitaAtualizacao() {
            ordemEm(StatusOrdemServico.ENTREGUE);

            assertThatThrownBy(() -> service.atualizar(100L,
                    new OrdemServicoAtualizacaoRequest("Laudo", null, null)))
                    .isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void rn09TecnicoInativoNaoPodeReceberOs() {
            OrdemServico os = ordemEm(StatusOrdemServico.AGUARDANDO_ANALISE);
            when(tecnicoService.obter(5L)).thenReturn(tecnico(false));

            assertThatThrownBy(() -> service.atribuirTecnico(100L, new AtribuicaoTecnicoRequest(5L)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("Técnico inativo não pode receber OS.");
            assertThat(os.getTecnico()).isNull();
        }

        @Test
        void rf13AtribuiTecnicoAtivo() {
            ordemEm(StatusOrdemServico.AGUARDANDO_ANALISE);
            when(tecnicoService.obter(5L)).thenReturn(tecnico(true));

            OrdemServicoResponse resposta = service.atribuirTecnico(100L, new AtribuicaoTecnicoRequest(5L));

            assertThat(resposta.tecnicoId()).isEqualTo(5L);
            assertThat(resposta.tecnicoNome()).isEqualTo("João");
        }

        @Test
        void rn08OsCanceladaNaoRecebeTecnico() {
            ordemEm(StatusOrdemServico.CANCELADO);

            assertThatThrownBy(() -> service.atribuirTecnico(100L, new AtribuicaoTecnicoRequest(5L)))
                    .isInstanceOf(RegraNegocioException.class);
            verify(tecnicoService, never()).obter(any());
        }
    }
}
