package br.com.fiap.cidadesesg.score;

import br.com.fiap.cidadesesg.domain.TipoIndicador;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Valor medido de um indicador em uma data de referência.
 */
public record Medicao(TipoIndicador tipo, double valor, LocalDate dataReferencia) {

    public Medicao {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(dataReferencia, "dataReferencia");
    }
}
