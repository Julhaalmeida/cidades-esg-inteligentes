package br.com.fiap.cidadesesg.web;

import br.com.fiap.cidadesesg.service.ScoreService;
import br.com.fiap.cidadesesg.web.dto.RankingItemResponse;
import br.com.fiap.cidadesesg.web.dto.ScoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Score ESG", description = "Cálculo do score ESG e ranking das cidades")
public class ScoreController {

    private final ScoreService service;

    public ScoreController(ScoreService service) {
        this.service = service;
    }

    @GetMapping("/cidades/{cidadeId}/score")
    @Operation(summary = "Calcula o score ESG de uma cidade",
            description = "Usa a medição mais recente de cada indicador, normalizada de 0 a 100, "
                    + "e faz a média ponderada das dimensões Ambiental, Social e Governança.")
    public ScoreResponse score(@PathVariable("cidadeId") Long cidadeId) {
        return service.calcular(cidadeId);
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking das cidades pelo score ESG geral")
    public List<RankingItemResponse> ranking() {
        return service.ranking();
    }
}
