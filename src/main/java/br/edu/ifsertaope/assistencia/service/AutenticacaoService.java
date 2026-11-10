package br.edu.ifsertaope.assistencia.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsertaope.assistencia.config.SegurancaProperties;
import br.edu.ifsertaope.assistencia.dto.LoginRequest;
import br.edu.ifsertaope.assistencia.dto.TokenResponse;
import br.edu.ifsertaope.assistencia.exception.CredenciaisInvalidasException;
import br.edu.ifsertaope.assistencia.mapper.UsuarioMapper;
import br.edu.ifsertaope.assistencia.model.Usuario;
import br.edu.ifsertaope.assistencia.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class AutenticacaoService {

    public static final String EMISSOR = "api-pedidos-assistencia";
    public static final String CLAIM_PERFIL = "perfil";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final SegurancaProperties propriedades;
    private final Clock clock;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder, SegurancaProperties propriedades, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
        this.clock = clock;
    }

    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByLoginIgnoreCase(UsuarioMapper.normalizarLogin(request.login()))
                .filter(Usuario::isAtivo)
                .filter(encontrado -> passwordEncoder.matches(request.senha(), encontrado.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);

        Instant emissao = clock.instant();
        Instant expiracao = emissao.plus(propriedades.jwt().expiracao());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getLogin())
                .issuedAt(emissao)
                .expiresAt(expiracao)
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .claim("nome", usuario.getNome())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();

        return new TokenResponse(token, "Bearer", LocalDateTime.ofInstant(expiracao, clock.getZone()),
                usuario.getLogin(), usuario.getPerfil());
    }
}
