package br.com.fiap.cidadesesg.web.dto;

import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.domain.TipoIndicador;

/** Item do catálogo de indicadores, com a faixa de referência usada na normalização. */
public record TipoIndicadorResponse(
        String codigo,
        Dimensao dimensao,
        String descricao,
        String unidade,
        double minimo,
        double maximo,
        boolean menorMelhor
) {

    public static TipoIndicadorResponse de(TipoIndicador tipo) {
        return new TipoIndicadorResponse(
                tipo.name(),
                tipo.getDimensao(),
                tipo.getDescricao(),
                tipo.getUnidade(),
                tipo.getMinimo(),
                tipo.getMaximo(),
                tipo.isMenorMelhor());
    }
}
