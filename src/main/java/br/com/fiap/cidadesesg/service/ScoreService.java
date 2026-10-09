package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.domain.IndicadorEsg;
import br.com.fiap.cidadesesg.repository.CidadeRepository;
import br.com.fiap.cidadesesg.repository.IndicadorEsgRepository;
import br.com.fiap.cidadesesg.score.EsgScoreCalculator;
import br.com.fiap.cidadesesg.score.Medicao;
import br.com.fiap.cidadesesg.score.ResultadoScore;
import br.com.fiap.cidadesesg.web.dto.RankingItemResponse;
import br.com.fiap.cidadesesg.web.dto.ScoreResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ScoreService {

    private final CidadeService cidadeService;
    private final CidadeRepository cidadeRepository;
    private final IndicadorEsgRepository indicadorRepository;
    private final EsgScoreCalculator calculadora;

    public ScoreService(CidadeService cidadeService,
                        CidadeRepository cidadeRepository,
                        IndicadorEsgRepository indicadorRepository,
                        EsgScoreCalculator calculadora) {
        this.cidadeService = cidadeService;
        this.cidadeRepository = cidadeRepository;
        this.indicadorRepository = indicadorRepository;
        this.calculadora = calculadora;
    }

    public ScoreResponse calcular(Long cidadeId) {
        Cidade cidade = cidadeService.obter(cidadeId);
        List<Medicao> medicoes = indicadorRepository.findByCidadeIdOrderByDataReferenciaDescIdDesc(cidadeId).stream()
                .map(IndicadorEsg::paraMedicao)
                .toList();
        return ScoreResponse.de(cidade, calculadora.calcular(medicoes), calculadora.getPesos(), Instant.now());
    }

    /**
     * Ranking de todas as cidades pelo score geral (maior primeiro).
     * Cidades sem indicadores aparecem no fim, com score nulo.
     */
    public List<RankingItemResponse> ranking() {
        // as cidades são carregadas antes para que os indicadores reutilizem as mesmas instâncias
        List<Cidade> cidades = cidadeRepository.findAll();
        Map<Long, List<Medicao>> medicoesPorCidade = indicadorRepository.findAllByOrderByDataReferenciaDescIdDesc()
                .stream()
                .collect(Collectors.groupingBy(
                        indicador -> indicador.getCidade().getId(),
                        Collectors.mapping(IndicadorEsg::paraMedicao, Collectors.toList())));

        record CidadePontuada(Cidade cidade, ResultadoScore resultado) {
        }

        List<CidadePontuada> ordenadas = cidades.stream()
                .map(cidade -> new CidadePontuada(cidade,
                        calculadora.calcular(medicoesPorCidade.getOrDefault(cidade.getId(), List.of()))))
                .sorted(Comparator
                        .comparing((CidadePontuada pontuada) -> pontuada.resultado().geral(),
                                Comparator.nullsLast(Comparator.<Double>reverseOrder()))
                        .thenComparing(pontuada -> pontuada.cidade().getNome()))
                .toList();

        List<RankingItemResponse> ranking = new ArrayList<>(ordenadas.size());
        for (int i = 0; i < ordenadas.size(); i++) {
            CidadePontuada pontuada = ordenadas.get(i);
            ranking.add(RankingItemResponse.de(i + 1, pontuada.cidade(), pontuada.resultado()));
        }
        return ranking;
    }
}
