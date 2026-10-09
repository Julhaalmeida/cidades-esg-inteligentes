package br.com.fiap.cidadesesg.web;

import br.com.fiap.cidadesesg.exception.RecursoDuplicadoException;
import br.com.fiap.cidadesesg.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadesesg.service.CidadeService;
import br.com.fiap.cidadesesg.web.dto.CidadeRequest;
import br.com.fiap.cidadesesg.web.dto.CidadeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CidadeController.class)
@DisplayName("API de cidades (camada web)")
class CidadeControllerTest {

    private static final Instant AGORA = Instant.parse("2026-01-15T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CidadeService service;

    @Test
    @DisplayName("GET /api/cidades lista as cidades")
    void lista() throws Exception {
        given(service.listar(null)).willReturn(List.of(
                new CidadeResponse(1L, "Curitiba", "PR", 1_773_000, AGORA, AGORA)));

        mockMvc.perform(get("/api/cidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Curitiba"))
                .andExpect(jsonPath("$[0].uf").value("PR"));
    }

    @Test
    @DisplayName("POST /api/cidades responde 201 com o cabeçalho Location")
    void cria() throws Exception {
        given(service.criar(any(CidadeRequest.class))).willReturn(
                new CidadeResponse(10L, "Porto Alegre", "RS", 1_332_000, AGORA, AGORA));

        mockMvc.perform(post("/api/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Porto Alegre", "uf": "RS", "populacao": 1332000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/cidades/10")))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST com dados inválidos responde 400 no formato Problem Details")
    void rejeitaDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "uf": "XX", "populacao": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET de cidade inexistente responde 404")
    void naoEncontrada() throws Exception {
        given(service.buscar(99L)).willThrow(new RecursoNaoEncontradoException("Cidade 99 não encontrada"));

        mockMvc.perform(get("/api/cidades/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Cidade 99 não encontrada"));
    }

    @Test
    @DisplayName("POST de cidade duplicada responde 409")
    void duplicada() throws Exception {
        given(service.criar(any(CidadeRequest.class)))
                .willThrow(new RecursoDuplicadoException("Já existe uma cidade chamada 'Recife' cadastrada em PE"));

        mockMvc.perform(post("/api/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Recife", "uf": "PE", "populacao": 1488000}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Recurso duplicado"));
    }

    @Test
    @DisplayName("DELETE responde 204 e GET com id inválido responde 400")
    void removeEValidaId() throws Exception {
        mockMvc.perform(delete("/api/cidades/5"))
                .andExpect(status().isNoContent());
        verify(service).remover(5L);

        mockMvc.perform(get("/api/cidades/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro inválido"));
    }

    @Test
    @DisplayName("DELETE de cidade inexistente responde 404")
    void removeInexistente() throws Exception {
        willThrow(new RecursoNaoEncontradoException("Cidade 42 não encontrada")).given(service).remover(42L);

        mockMvc.perform(delete("/api/cidades/42"))
                .andExpect(status().isNotFound());
    }
}
