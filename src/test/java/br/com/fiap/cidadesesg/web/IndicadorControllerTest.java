package br.com.fiap.cidadesesg.web;

import br.com.fiap.cidadesesg.exception.RegraDeNegocioException;
import br.com.fiap.cidadesesg.service.IndicadorService;
import br.com.fiap.cidadesesg.web.dto.IndicadorRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IndicadorController.class)
@DisplayName("API de indicadores (camada web)")
class IndicadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndicadorService service;

    @Test
    @DisplayName("GET /api/indicadores/tipos devolve o catálogo com os 9 indicadores")
    void catalogo() throws Exception {
        mockMvc.perform(get("/api/indicadores/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[0].codigo").value("EMISSOES_CO2_PER_CAPITA"))
                .andExpect(jsonPath("$[0].menorMelhor").value(true));
    }

    @Test
    @DisplayName("tipo de indicador desconhecido responde 400")
    void tipoDesconhecido() throws Exception {
        mockMvc.perform(post("/api/cidades/1/indicadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "INDICADOR_INEXISTENTE", "valor": 10, "dataReferencia": "2025-12-31"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição malformada"));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("data de referência no futuro responde 400")
    void dataNoFuturo() throws Exception {
        String amanha = LocalDate.now().plusDays(2).toString();

        mockMvc.perform(post("/api/cidades/1/indicadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "ENERGIA_RENOVAVEL", "valor": 10, "dataReferencia": "%s"}
                                """.formatted(amanha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("violação de regra de negócio responde 422")
    void regraDeNegocio() throws Exception {
        given(service.registrar(eq(1L), any(IndicadorRequest.class)))
                .willThrow(new RegraDeNegocioException("O indicador ENERGIA_RENOVAVEL é percentual e não pode passar de 100"));

        mockMvc.perform(post("/api/cidades/1/indicadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "ENERGIA_RENOVAVEL", "valor": 120, "dataReferencia": "2025-12-31"}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }
}
