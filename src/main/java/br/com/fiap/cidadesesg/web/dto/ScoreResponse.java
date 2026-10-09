package br.com.fiap.cidadesesg.web.dto;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.score.Classificacao;
import br.com.fiap.cidadesesg.score.PesosEsg;
import br.com.fiap.cidadesesg.score.ResultadoScore;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Score ESG de uma cidade. As dimensões sem indicadores aparecem com valor {@code null}.
 */
public record ScoreResponse(
        Long cidadeId,
        String cidade,
        String uf,
        Double scoreGeral,
        String classificacao,
        String descricaoClassificacao,
        Map<Dimensao, Double> dimensoes,
        Map<Dimensao, Double> pesos,
        int indicadoresConsiderados,
        Instant calculadoEm
) {

    public static ScoreResponse de(Cidade cidade, ResultadoScore resultado, PesosEsg pesos, Instant calculadoEm) {
        Map<Dimensao, Double> dimensoes = new EnumMap<>(Dimensao.class);
        Map<Dimensao, Double> pesosPorDimensao = new EnumMap<>(Dimensao.class);
        for (Dimensao dimensao : Dimensao.values()) {
            dimensoes.put(dimensao, resultado.porDimensao().get(dimensao));
            pesosPorDimensao.put(dimensao, pesos.de(dimensao));
        }
        Classificacao classificacao = resultado.classificacao();
        return new ScoreResponse(
                cidade.getId(),
                cidade.getNome(),
                cidade.getUf(),
                resultado.geral(),
                classificacao == null ? null : classificacao.name(),
                classificacao == null ? "Sem dados suficientes" : classificacao.getDescricao(),
                dimensoes,
                pesosPorDimensao,
                resultado.indicadoresConsiderados(),
                calculadoEm);
    }
}
