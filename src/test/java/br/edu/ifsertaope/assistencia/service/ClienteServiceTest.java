package br.edu.ifsertaope.assistencia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifsertaope.assistencia.dto.ClienteRequest;
import br.edu.ifsertaope.assistencia.dto.ClienteResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.repository.ClienteRepository;
import br.edu.ifsertaope.assistencia.repository.EquipamentoRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-09T17:30:00Z"), ZoneId.of("America/Recife"));

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    private ClienteService service;

    @BeforeEach
    void configurar() {
        service = new ClienteService(clienteRepository, equipamentoRepository, CLOCK);
    }

    private Cliente clienteSalvo() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        cliente.setTelefone("(87) 99999-0000");
        cliente.setEmail("maria@email.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        return cliente;
    }

    @Test
    void rf01CriaClienteNormalizandoDadosEPreenchendoDataDeCadastro() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse resposta = service.criar(
                new ClienteRequest(" Maria ", "(87) 99999-0000", " Maria@Email.com ", " "));

        assertThat(resposta.nome()).isEqualTo("Maria");
        assertThat(resposta.email()).isEqualTo("maria@email.com");
        assertThat(resposta.cpfCnpj()).isNull();
        assertThat(resposta.dataCadastro()).isEqualTo(LocalDateTime.of(2026, 11, 9, 14, 30));
    }

    @Test
    void rn06EmailDuplicadoRetornaConflito() {
        when(clienteRepository.existsByEmailIgnoreCase("maria@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new ClienteRequest("Maria", "1", "maria@email.com", null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um cliente com este e-mail.");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void rn06CpfCnpjDuplicadoRetornaConflito() {
        when(clienteRepository.existsByCpfCnpj("123.456.789-09")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new ClienteRequest("Maria", "1", "maria@email.com", "123.456.789-09")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um cliente com este CPF/CNPJ.");
    }

    @Test
    void rn06AtualizacaoIgnoraOProprioClienteNaVerificacaoDeUnicidade() {
        clienteSalvo();

        ClienteResponse resposta = service.atualizar(1L,
                new ClienteRequest("Maria Souza", "(87) 98888-0000", "maria@email.com", null));

        verify(clienteRepository).existsByEmailIgnoreCaseAndIdNot("maria@email.com", 1L);
        assertThat(resposta.nome()).isEqualTo("Maria Souza");
    }

    @Test
    void rn06AtualizacaoComEmailDeOutroClienteRetornaConflito() {
        clienteSalvo();
        when(clienteRepository.existsByEmailIgnoreCaseAndIdNot("outra@email.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new ClienteRequest("Maria", "1", "outra@email.com", null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void rn07ClienteComEquipamentosNaoPodeSerRemovido() {
        Cliente cliente = clienteSalvo();
        when(equipamentoRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Cliente possui equipamentos cadastrados e não pode ser removido.");
        verify(clienteRepository, never()).delete(cliente);
    }

    @Test
    void rn07ClienteSemEquipamentosEhRemovido() {
        Cliente cliente = clienteSalvo();

        service.remover(1L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    void clienteInexistenteRetornaNaoEncontrado() {
        when(clienteRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(9L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente com id 9 não encontrado.");
    }
}
