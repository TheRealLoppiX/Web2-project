package br.edu.ifsertaope.assistencia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifsertaope.assistencia.dto.EquipamentoRequest;
import br.edu.ifsertaope.assistencia.dto.EquipamentoResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.model.Equipamento;
import br.edu.ifsertaope.assistencia.model.TipoEquipamento;
import br.edu.ifsertaope.assistencia.repository.EquipamentoRepository;
import br.edu.ifsertaope.assistencia.repository.OrdemServicoRepository;

@ExtendWith(MockitoExtension.class)
class EquipamentoServiceTest {

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ClienteService clienteService;

    private EquipamentoService service;

    private Cliente cliente;

    @BeforeEach
    void configurar() {
        service = new EquipamentoService(equipamentoRepository, ordemServicoRepository, clienteService);
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
    }

    private EquipamentoRequest requisicao(Long clienteId) {
        return new EquipamentoRequest(clienteId, TipoEquipamento.NOTEBOOK, "Dell", "Inspiron", " ", "i5, 8 GB");
    }

    private Equipamento equipamentoSalvo() {
        Equipamento equipamento = new Equipamento();
        equipamento.setId(10L);
        equipamento.setCliente(cliente);
        when(equipamentoRepository.findById(10L)).thenReturn(Optional.of(equipamento));
        return equipamento;
    }

    @Test
    void rf05CriaEquipamentoVinculadoAoCliente() {
        when(clienteService.obter(1L)).thenReturn(cliente);
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipamentoResponse resposta = service.criar(requisicao(1L));

        assertThat(resposta.clienteId()).isEqualTo(1L);
        assertThat(resposta.clienteNome()).isEqualTo("Maria");
        assertThat(resposta.numeroSerie()).isNull();
        assertThat(resposta.especificacoes()).isEqualTo("i5, 8 GB");
    }

    @Test
    void clienteInexistenteImpedeCadastro() {
        when(clienteService.obter(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente com id 99 não encontrado."));

        assertThatThrownBy(() -> service.criar(requisicao(99L))).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(equipamentoRepository, never()).save(any());
    }

    @Test
    void rn07EquipamentoComOsNaoPodeSerRemovido() {
        Equipamento equipamento = equipamentoSalvo();
        when(ordemServicoRepository.existsByEquipamentoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(10L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Equipamento possui ordens de serviço e não pode ser removido.");
        verify(equipamentoRepository, never()).delete(equipamento);
    }

    @Test
    void rn07EquipamentoSemOsEhRemovido() {
        Equipamento equipamento = equipamentoSalvo();

        service.remover(10L);

        verify(equipamentoRepository).delete(equipamento);
    }

    @Test
    void rf06ListarEquipamentosDeClienteInexistenteRetornaNaoEncontrado() {
        when(clienteService.obter(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente com id 99 não encontrado."));

        assertThatThrownBy(() -> service.listarPorCliente(99L, null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
