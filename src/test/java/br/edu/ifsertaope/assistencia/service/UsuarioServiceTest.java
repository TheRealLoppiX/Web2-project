package br.edu.ifsertaope.assistencia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.ifsertaope.assistencia.dto.UsuarioRequest;
import br.edu.ifsertaope.assistencia.dto.UsuarioResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.model.Perfil;
import br.edu.ifsertaope.assistencia.model.Usuario;
import br.edu.ifsertaope.assistencia.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService service;

    @BeforeEach
    void configurar() {
        service = new UsuarioService(usuarioRepository, ENCODER);
    }

    private Usuario salvo() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void criaUsuarioComSenhaCriptografadaELoginNormalizado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resposta = service.criar(
                new UsuarioRequest("Carla", " Carla.Atendimento ", "senha-segura-123", Perfil.ATENDENTE));

        Usuario usuario = salvo();
        assertThat(usuario.getSenhaHash()).isNotEqualTo("senha-segura-123");
        assertThat(ENCODER.matches("senha-segura-123", usuario.getSenhaHash())).isTrue();
        assertThat(resposta.login()).isEqualTo("carla.atendimento");
        assertThat(resposta.ativo()).isTrue();
    }

    @Test
    void loginDuplicadoRetornaConflito() {
        when(usuarioRepository.existsByLoginIgnoreCase("carla")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest("Carla", "carla", "senha-segura-123",
                Perfil.TECNICO)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um usuário com este login.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criaGerenteInicialQuandoNaoHaUsuarios() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.criarGerenteInicialSeNecessario("Gerente", "gerente", "senha-inicial")).isTrue();
        assertThat(salvo().getPerfil()).isEqualTo(Perfil.GERENTE);
    }

    @Test
    void naoRecriaGerenteQuandoJaExistemUsuarios() {
        when(usuarioRepository.count()).thenReturn(3L);

        assertThat(service.criarGerenteInicialSeNecessario("Gerente", "gerente", null)).isFalse();
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void exigeSenhaDoGerenteInicialQuandoBancoVazio() {
        assertThatThrownBy(() -> service.criarGerenteInicialSeNecessario("Gerente", "gerente", " "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_PASSWORD");
    }
}
