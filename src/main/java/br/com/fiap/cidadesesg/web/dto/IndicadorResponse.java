package br.com.fiap.cidadesesg.web.dto;

import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.domain.IndicadorEsg;
import br.com.fiap.cidadesesg.domain.TipoIndicador;
import br.com.fiap.cidadesesg.score.EsgScoreCalculator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Medição registrada. {@code notaNormalizada} é o valor convertido para a escala de 0 a 100
 * usada no cálculo do score.
 */
public record IndicadorResponse(
        Long id,
        Long cidadeId,
        TipoIndicador tipo,
        Dimensao dimensao,
        String descricao,
        String unidade,
        BigDecimal valor,
        double notaNormalizada,
        LocalDate dataReferencia,
        String fonte,
        Instant criadoEm
) {

    public static IndicadorResponse de(IndicadorEsg indicador) {
        TipoIndicador tipo = indicador.getTipo();
        return new IndicadorResponse(
                indicador.getId(),
                indicador.getCidade().getId(),
                tipo,
                tipo.getDimensao(),
                tipo.getDescricao(),
                tipo.getUnidade(),
                indicador.getValor(),
                EsgScoreCalculator.normalizar(tipo, indicador.getValor().doubleValue()),
                indicador.getDataReferencia(),
                indicador.getFonte(),
                indicador.getCriadoEm());
    }
}
