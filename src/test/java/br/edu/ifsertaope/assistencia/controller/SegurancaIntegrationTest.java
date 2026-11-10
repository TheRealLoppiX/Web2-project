package br.edu.ifsertaope.assistencia.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaIntegrationTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, String token, Object corpo)
            throws Exception {
        if (token != null) {
            requisicao.header(HttpHeaders.AUTHORIZATION, Autenticacao.bearer(token));
        }
        return mockMvc.perform(requisicao
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(corpo)));
    }

    private String tokenGerente() throws Exception {
        return Autenticacao.token(mockMvc, objectMapper, Autenticacao.GERENTE, Autenticacao.SENHA_GERENTE);
    }

    private String criarUsuarioELogar(String perfil) throws Exception {
        String login = perfil.toLowerCase() + SEQUENCIA.incrementAndGet();
        enviar(post("/api/usuarios"), tokenGerente(), Map.of(
                "nome", "Usuário " + perfil, "login", login, "senha", "senha-segura-123", "perfil", perfil))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value(perfil))
                .andExpect(jsonPath("$.senha").doesNotExist());
        return Autenticacao.token(mockMvc, objectMapper, login, "senha-segura-123");
    }

    @Test
    void loginValidoDevolveTokenBearerComPerfil() throws Exception {
        enviar(post("/api/auth/login"), null, Map.of("login", "GERENTE", "senha", Autenticacao.SENHA_GERENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.perfil").value("GERENTE"))
                .andExpect(jsonPath("$.expiraEm", notNullValue()));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        enviar(post("/api/auth/login"), null, Map.of("login", "gerente", "senha", "errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Não autenticado"))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos."));
    }

    @Test
    void loginSemCamposRetorna400() throws Exception {
        enviar(post("/api/auth/login"), null, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(2));
    }

    @Test
    void requisicaoSemTokenRetorna401NoFormatoPadrao() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.caminho").value("/api/clientes"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Autenticação necessária. Faça login em /api/auth/login e envie o token no cabeçalho Authorization."));
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        String token = tokenGerente();
        String adulterado = token.substring(0, token.length() - 4) + "abcd";

        mockMvc.perform(get("/api/clientes").header(HttpHeaders.AUTHORIZATION, Autenticacao.bearer(adulterado)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Token inválido ou expirado. Faça login novamente."));
    }

    @Test
    void documentacaoSwaggerEhPublica() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void atendenteCadastraClienteMasNaoGerenciaTecnicos() throws Exception {
        String atendente = criarUsuarioELogar("ATENDENTE");

        enviar(post("/api/clientes"), atendente, Map.of(
                "nome", "Cliente do balcão", "telefone", "(87) 90000-0000", "email", "balcao@email.com"))
                .andExpect(status().isCreated());
        enviar(post("/api/tecnicos"), atendente, Map.of(
                "nome", "Técnico", "email", "tec@assistencia.com", "especialidade", "hardware"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.mensagem").value("Seu perfil não tem permissão para esta operação."));
        enviar(put("/api/ordens-servico/1"), atendente, Map.of("laudoTecnico", "x"))
                .andExpect(status().isForbidden());
        enviar(patch("/api/ordens-servico/1/tecnico"), atendente, Map.of("tecnicoId", 1))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, Autenticacao.bearer(atendente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void tecnicoRegistraLaudoMasNaoCadastraCliente() throws Exception {
        String tecnico = criarUsuarioELogar("TECNICO");

        enviar(post("/api/clientes"), tecnico, Map.of("nome", "X", "telefone", "1", "email", "x@email.com"))
                .andExpect(status().isForbidden());
        enviar(put("/api/ordens-servico/999999"), tecnico, Map.of("laudoTecnico", "Laudo"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/clientes").header(HttpHeaders.AUTHORIZATION, Autenticacao.bearer(tecnico)))
                .andExpect(status().isOk());
    }

    @Test
    void loginDuplicadoRetorna409() throws Exception {
        enviar(post("/api/usuarios"), tokenGerente(), Map.of(
                "nome", "Outro gerente", "login", "Gerente", "senha", "senha-segura-123", "perfil", "GERENTE"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um usuário com este login."));
    }
}
