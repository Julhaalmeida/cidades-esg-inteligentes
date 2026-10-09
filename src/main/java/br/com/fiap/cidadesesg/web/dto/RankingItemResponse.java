package br.com.fiap.cidadesesg.web.dto;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.score.Classificacao;
import br.com.fiap.cidadesesg.score.ResultadoScore;

/** Posição de uma cidade no ranking ESG. */
public record RankingItemResponse(
        int posicao,
        Long cidadeId,
        String cidade,
        String uf,
        Double scoreGeral,
        String classificacao,
        String descricaoClassificacao
) {

    public static RankingItemResponse de(int posicao, Cidade cidade, ResultadoScore resultado) {
        Classificacao classificacao = resultado.classificacao();
        return new RankingItemResponse(
                posicao,
                cidade.getId(),
                cidade.getNome(),
                cidade.getUf(),
                resultado.geral(),
                classificacao == null ? null : classificacao.name(),
                classificacao == null ? "Sem dados suficientes" : classificacao.getDescricao());
    }
}
