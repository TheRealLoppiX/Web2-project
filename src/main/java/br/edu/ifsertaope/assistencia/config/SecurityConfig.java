package br.edu.ifsertaope.assistencia.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import br.edu.ifsertaope.assistencia.exception.RespostaErroSeguranca;
import br.edu.ifsertaope.assistencia.service.AutenticacaoService;

@Configuration
@EnableConfigurationProperties(SegurancaProperties.class)
public class SecurityConfig {

    private static final String ATENDENTE = "ATENDENTE";
    private static final String TECNICO = "TECNICO";
    private static final String GERENTE = "GERENTE";

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http, RespostaErroSeguranca respostaErro)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error")
                        .permitAll()
                        .requestMatchers("/api/usuarios/**").hasRole(GERENTE)
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/clientes/**", "/api/equipamentos/**").hasAnyRole(ATENDENTE, GERENTE)
                        .requestMatchers("/api/tecnicos/**").hasRole(GERENTE)
                        .requestMatchers(HttpMethod.POST, "/api/ordens-servico").hasAnyRole(ATENDENTE, GERENTE)
                        .requestMatchers(HttpMethod.PUT, "/api/ordens-servico/*").hasAnyRole(TECNICO, GERENTE)
                        .requestMatchers(HttpMethod.PATCH, "/api/ordens-servico/*/tecnico").hasRole(GERENTE)
                        .requestMatchers(HttpMethod.PATCH, "/api/ordens-servico/*/status")
                        .hasAnyRole(ATENDENTE, TECNICO, GERENTE)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePerfis()))
                        .authenticationEntryPoint(respostaErro)
                        .accessDeniedHandler(respostaErro))
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(respostaErro)
                        .accessDeniedHandler(respostaErro))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(SegurancaProperties propriedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave(propriedades)));
    }

    @Bean
    public JwtDecoder jwtDecoder(SegurancaProperties propriedades) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave(propriedades))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(AutenticacaoService.EMISSOR));
        return decoder;
    }

    private static JwtAuthenticationConverter conversorDePerfis() {
        JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
        perfis.setAuthoritiesClaimName(AutenticacaoService.CLAIM_PERFIL);
        perfis.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(perfis);
        return conversor;
    }

    private static SecretKey chave(SegurancaProperties propriedades) {
        return new SecretKeySpec(propriedades.jwt().segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
