package br.edu.ifsertaope.assistencia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import br.edu.ifsertaope.assistencia.config.SegurancaProperties;
import br.edu.ifsertaope.assistencia.dto.LoginRequest;
import br.edu.ifsertaope.assistencia.dto.TokenResponse;
import br.edu.ifsertaope.assistencia.exception.CredenciaisInvalidasException;
import br.edu.ifsertaope.assistencia.model.Perfil;
import br.edu.ifsertaope.assistencia.model.Usuario;
import br.edu.ifsertaope.assistencia.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-pelo-menos-32-bytes";
    private static final SecretKeySpec CHAVE =
            new SecretKeySpec(SEGREDO.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock
    private UsuarioRepository usuarioRepository;

    private AutenticacaoService service;

    @BeforeEach
    void configurar() {
        SegurancaProperties propriedades = new SegurancaProperties(
                new SegurancaProperties.Jwt(SEGREDO, Duration.ofHours(8)),
                new SegurancaProperties.Administrador("Gerente", "gerente", "x"));
        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("America/Recife"));
        service = new AutenticacaoService(usuarioRepository, ENCODER,
                new NimbusJwtEncoder(new ImmutableSecret<>(CHAVE)), propriedades, clock);
    }

    private Usuario usuario(boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setLogin("carla");
        usuario.setNome("Carla");
        usuario.setPerfil(Perfil.ATENDENTE);
        usuario.setSenhaHash(ENCODER.encode("senha-correta"));
        usuario.setAtivo(ativo);
        when(usuarioRepository.findByLoginIgnoreCase("carla")).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test
    void loginValidoGeraTokenAssinadoComPerfilEExpiracao() {
        usuario(true);

        TokenResponse resposta = service.login(new LoginRequest(" Carla ", "senha-correta"));

        Jwt jwt = NimbusJwtDecoder.withSecretKey(CHAVE).macAlgorithm(MacAlgorithm.HS256).build()
                .decode(resposta.token());
        assertThat(jwt.getSubject()).isEqualTo("carla");
        assertThat(jwt.getClaimAsString("perfil")).isEqualTo("ATENDENTE");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(AutenticacaoService.EMISSOR);
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(8));
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.perfil()).isEqualTo(Perfil.ATENDENTE);
        assertThat(resposta.expiraEm()).isAfter(LocalDateTime.now(ZoneId.of("America/Recife")).plusHours(7));
    }

    @Test
    void senhaErradaEhRejeitada() {
        usuario(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("carla", "senha-errada")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Login ou senha inválidos.");
    }

    @Test
    void usuarioInativoNaoAutentica() {
        usuario(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("carla", "senha-correta")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void usuarioInexistenteRecebeMesmaMensagem() {
        when(usuarioRepository.findByLoginIgnoreCase("ninguem")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ninguem", "qualquer")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Login ou senha inválidos.");
    }

    @Test
    void segredoCurtoImpedeInicializacao() {
        assertThatThrownBy(() -> new SegurancaProperties(new SegurancaProperties.Jwt("curto", Duration.ofHours(1)),
                null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
