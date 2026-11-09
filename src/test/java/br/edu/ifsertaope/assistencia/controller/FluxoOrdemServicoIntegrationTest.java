package br.edu.ifsertaope.assistencia.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoOrdemServicoIntegrationTest {

    private static final String DATA_ISO_8601 = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, Object corpo) throws Exception {
        return mockMvc.perform(requisicao
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(corpo)));
    }

    private long id(ResultActions resultado) throws Exception {
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private long criarCliente(String email) throws Exception {
        return id(enviar(post("/api/clientes"), Map.of(
                "nome", "Maria da Silva",
                "telefone", "(87) 99999-0000",
                "email", email))
                .andExpect(status().isCreated()));
    }

    private long criarEquipamento(long clienteId) throws Exception {
        return id(enviar(post("/api/equipamentos"), Map.of(
                "clienteId", clienteId,
                "tipo", "NOTEBOOK",
                "marca", "Dell",
                "modelo", "Inspiron 15"))
                .andExpect(status().isCreated()));
    }

    private long abrirOs(long equipamentoId) throws Exception {
        return id(enviar(post("/api/ordens-servico"), Map.of(
                "equipamentoId", equipamentoId,
                "descricaoDefeito", "Não liga"))
                .andExpect(status().isCreated()));
    }

    private ResultActions mudarStatus(long osId, String novoStatus) throws Exception {
        return enviar(patch("/api/ordens-servico/{id}/status", osId), Map.of("status", novoStatus));
    }

    @Test
    void fluxoCompletoDaOrdemDeServicoAteEntregue() throws Exception {
        long clienteId = id(enviar(post("/api/clientes"), Map.of(
                "nome", "Maria da Silva",
                "telefone", "(87) 99999-0000",
                "email", "maria@email.com",
                "cpfCnpj", "123.456.789-09"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.dataCadastro", notNullValue())));

        enviar(post("/api/clientes"), Map.of(
                "nome", "Outra Maria",
                "telefone", "(87) 98888-0000",
                "email", "MARIA@email.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um cliente com este e-mail."));

        long equipamentoId = id(enviar(post("/api/equipamentos"), Map.of(
                "clienteId", clienteId,
                "tipo", "NOTEBOOK",
                "marca", "Dell",
                "modelo", "Inspiron 15",
                "numeroSerie", "BR-5CD1234XYZ"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clienteId)));

        mockMvc.perform(get("/api/clientes/{id}/equipamentos", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.tamanho").value(20));

        long tecnicoId = id(enviar(post("/api/tecnicos"), Map.of(
                "nome", "João Pereira",
                "email", "joao@assistencia.com",
                "especialidade", "hardware"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativo").value(true)));

        long osId = id(enviar(post("/api/ordens-servico"), Map.of(
                "equipamentoId", equipamentoId,
                "descricaoDefeito", "Notebook não liga"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ANALISE"))
                .andExpect(jsonPath("$.dataAbertura", matchesPattern(DATA_ISO_8601))));

        enviar(put("/api/ordens-servico/{id}", osId), Map.of(
                "laudoTecnico", "Cooler obstruído e pasta térmica ressecada.",
                "valorTotal", 150.00))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(150.0));

        enviar(patch("/api/ordens-servico/{id}/status", osId), Map.of(
                "status", "ORCAMENTO_APROVADO",
                "observacao", "Cliente aprovou por telefone."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ORCAMENTO_APROVADO"));

        enviar(patch("/api/ordens-servico/{id}/tecnico", osId), Map.of("tecnicoId", tecnicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoId").value(tecnicoId));

        mudarStatus(osId, "EM_REPARO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_REPARO"));

        enviar(put("/api/ordens-servico/{id}", osId),
                Map.of("descricaoServicos", "Limpeza interna e troca de pasta térmica."))
                .andExpect(status().isOk());

        mudarStatus(osId, "CONCLUIDO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataConclusao", notNullValue()))
                .andExpect(jsonPath("$.dataEntrega", nullValue()));

        mudarStatus(osId, "ENTREGUE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.dataEntrega", notNullValue()));

        mockMvc.perform(get("/api/ordens-servico/{id}/historico", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].statusNovo",
                        contains("AGUARDANDO_ANALISE", "ORCAMENTO_APROVADO", "EM_REPARO", "CONCLUIDO", "ENTREGUE")))
                .andExpect(jsonPath("$[0].statusAnterior", nullValue()));

        mockMvc.perform(get("/api/ordens-servico")
                        .param("status", "ENTREGUE")
                        .param("clienteId", String.valueOf(clienteId))
                        .param("tecnicoId", String.valueOf(tecnicoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].id").value(osId));

        enviar(put("/api/ordens-servico/{id}", osId), Map.of("laudoTecnico", "Alteração tardia"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Ordem de serviço com status ENTREGUE não aceita mais alterações."));

        mockMvc.perform(delete("/api/equipamentos/{id}", equipamentoId))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/clientes/{id}", clienteId))
                .andExpect(status().isConflict());
    }

    @Test
    void transicaoInvalidaRetorna422NoFormatoPadrao() throws Exception {
        long osId = abrirOs(criarEquipamento(criarCliente("transicao@email.com")));

        mudarStatus(osId, "ENTREGUE")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.erro").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.mensagem").value("Transição de AGUARDANDO_ANALISE para ENTREGUE não permitida."))
                .andExpect(jsonPath("$.caminho").value("/api/ordens-servico/" + osId + "/status"))
                .andExpect(jsonPath("$.timestamp", matchesPattern(DATA_ISO_8601)))
                .andExpect(jsonPath("$.campos").doesNotExist());

        mockMvc.perform(get("/api/ordens-servico/{id}", osId))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ANALISE"));
    }

    @Test
    void validacaoRetorna400ComCamposInvalidos() throws Exception {
        enviar(post("/api/clientes"), Map.of("nome", "Sem telefone", "email", "invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição inválida"))
                .andExpect(jsonPath("$.campos[?(@.campo == 'telefone')].mensagem")
                        .value("O campo telefone é obrigatório."))
                .andExpect(jsonPath("$.campos[?(@.campo == 'email')].mensagem")
                        .value("O campo email deve conter um e-mail válido."));
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/ordens-servico/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Ordem de serviço com id 999999 não encontrada."));
    }

    @Test
    void tecnicoDesativadoNaoRecebeOs() throws Exception {
        long osId = abrirOs(criarEquipamento(criarCliente("inativo@email.com")));
        long tecnicoId = id(enviar(post("/api/tecnicos"), Map.of("nome", "Ana", "email", "ana@assistencia.com"))
                .andExpect(status().isCreated()));

        mockMvc.perform(delete("/api/tecnicos/{id}", tecnicoId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/tecnicos").param("ativos", "false"))
                .andExpect(jsonPath("$.conteudo[?(@.id == " + tecnicoId + ")].ativo").value(false));

        enviar(patch("/api/ordens-servico/{id}/tecnico", osId), Map.of("tecnicoId", tecnicoId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Técnico inativo não pode receber OS."));
    }
}
