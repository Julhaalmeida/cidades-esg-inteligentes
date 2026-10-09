package br.com.fiap.cidadesesg.score;

import br.com.fiap.cidadesesg.domain.Dimensao;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Resultado do cálculo do score ESG de uma cidade.
 *
 * @param geral                   score geral de 0 a 100 ({@code null} quando não há dados)
 * @param classificacao           faixa do score geral ({@code null} quando não há dados)
 * @param porDimensao             nota de cada dimensão que possui indicadores
 * @param indicadoresConsiderados quantidade de indicadores distintos usados no cálculo
 */
public record ResultadoScore(Double geral,
                             Classificacao classificacao,
                             Map<Dimensao, Double> porDimensao,
                             int indicadoresConsiderados) {

    public ResultadoScore {
        EnumMap<Dimensao, Double> copia = new EnumMap<>(Dimensao.class);
        if (porDimensao != null) {
            copia.putAll(porDimensao);
        }
        porDimensao = Collections.unmodifiableMap(copia);
    }

    public static ResultadoScore semDados() {
        return new ResultadoScore(null, null, Map.of(), 0);
    }

    public boolean possuiDados() {
        return geral != null;
    }
}
