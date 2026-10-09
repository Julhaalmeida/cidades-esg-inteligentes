package br.com.fiap.cidadesesg;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração: sobe a aplicação completa contra um PostgreSQL real
 * (no pipeline, o "service container" do GitHub Actions), aplica as migrações
 * do Flyway e exercita a API de ponta a ponta.
 *
 * <p>Conexão definida pelas variáveis DB_HOST, DB_PORT, DB_NAME, DB_USER e DB_PASSWORD.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API completa com PostgreSQL (integração)")
class CidadesEsgApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("as migrações do Flyway criam as tabelas e os dados de demonstração")
    void migracoesComDadosDeDemonstracao() throws Exception {
        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[0].scoreGeral").isNumber());
    }

    @Test
    @DisplayName("fluxo completo: cadastrar cidade, registrar indicadores, calcular score e remover")
    void fluxoCompleto() throws Exception {
        String nome = "Cidade Teste " + System.nanoTime();

        MvcResult criada = mockMvc.perform(post("/api/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "%s", "uf": "SP", "populacao": 250000}
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(criada.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/cidades/{id}/indicadores", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "ENERGIA_RENOVAVEL", "valor": 80, "dataReferencia": "2025-12-31", "fonte": "Teste"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notaNormalizada").value(80.0));

        mockMvc.perform(post("/api/cidades/{id}/indicadores", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo": "TRANSPARENCIA_PUBLICA", "valor": 90, "dataReferencia": "2025-12-31"}
                                """))
                .andExpect(status().isCreated());

        // (0,4 x 80 + 0,3 x 90) / 0,7 = 84,29 -> classe A
        mockMvc.perform(get("/api/cidades/{id}/score", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scoreGeral").value(84.3))
                .andExpect(jsonPath("$.classificacao").value("A"))
                .andExpect(jsonPath("$.dimensoes.AMBIENTAL").value(80.0))
                .andExpect(jsonPath("$.indicadoresConsiderados").value(2));

        mockMvc.perform(post("/api/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "%s", "uf": "sp", "populacao": 1}
                                """.formatted(nome.toUpperCase())))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/cidades/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/cidades/{id}", id))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/cidades/{id}/indicadores", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("health check informa aplicação e banco no ar")
    void healthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("/api/info identifica o ambiente em execução")
    void info() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente").value("teste"))
                .andExpect(jsonPath("$.aplicacao").value("Cidades ESG Inteligentes"));
    }
}
