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

import br.edu.ifsertaope.assistencia.dto.TecnicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.model.Tecnico;
import br.edu.ifsertaope.assistencia.repository.TecnicoRepository;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {

    @Mock
    private TecnicoRepository tecnicoRepository;

    private TecnicoService service;

    @BeforeEach
    void configurar() {
        service = new TecnicoService(tecnicoRepository);
    }

    private Tecnico tecnicoSalvo() {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(5L);
        tecnico.setNome("João");
        tecnico.setEmail("joao@assistencia.com");
        tecnico.setEspecialidade("hardware");
        when(tecnicoRepository.findById(5L)).thenReturn(Optional.of(tecnico));
        return tecnico;
    }

    @Test
    void rf08CriaTecnicoAtivo() {
        when(tecnicoRepository.save(any(Tecnico.class))).thenAnswer(inv -> inv.getArgument(0));

        TecnicoResponse resposta = service.criar(new TecnicoRequest("João", "Joao@Assistencia.com", "hardware"));

        assertThat(resposta.ativo()).isTrue();
        assertThat(resposta.email()).isEqualTo("joao@assistencia.com");
    }

    @Test
    void rn06EmailDeTecnicoDuplicadoRetornaConflito() {
        when(tecnicoRepository.existsByEmailIgnoreCase("joao@assistencia.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new TecnicoRequest("João", "joao@assistencia.com", "hardware")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um técnico com este e-mail.");
        verify(tecnicoRepository, never()).save(any());
    }

    @Test
    void rn06AtualizacaoComEmailDeOutroTecnicoRetornaConflito() {
        tecnicoSalvo();
        when(tecnicoRepository.existsByEmailIgnoreCaseAndIdNot("ana@assistencia.com", 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(5L,
                new TecnicoAtualizacaoRequest("João", "ana@assistencia.com", "hardware", null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void rf10DesativarFazExclusaoLogica() {
        Tecnico tecnico = tecnicoSalvo();

        service.desativar(5L);

        assertThat(tecnico.isAtivo()).isFalse();
        verify(tecnicoRepository, never()).delete(any());
    }

    @Test
    void rf10AtualizacaoPodeReativarTecnico() {
        Tecnico tecnico = tecnicoSalvo();
        tecnico.setAtivo(false);

        service.atualizar(5L, new TecnicoAtualizacaoRequest("João", "joao@assistencia.com", "software", true));

        assertThat(tecnico.isAtivo()).isTrue();
        assertThat(tecnico.getEspecialidade()).isEqualTo("software");
    }
}
