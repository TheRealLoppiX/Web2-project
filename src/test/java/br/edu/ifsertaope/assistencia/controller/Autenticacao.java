package br.edu.ifsertaope.assistencia.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

final class Autenticacao {

    static final String GERENTE = "gerente";
    static final String SENHA_GERENTE = "senha-do-gerente";

    private Autenticacao() {
    }

    static String token(MockMvc mockMvc, ObjectMapper objectMapper, String login, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("login", login, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }

    static MockMvc mockMvcComToken(WebApplicationContext contexto, String token) {
        return MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .build();
    }
}
