package br.com.fiap.cidadesesg.web.dto;

import br.com.fiap.cidadesesg.domain.TipoIndicador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados para registrar a medição de um indicador ESG. */
public record IndicadorRequest(

        @Schema(description = "Tipo do indicador (veja GET /api/indicadores/tipos)", example = "ENERGIA_RENOVAVEL")
        @NotNull(message = "O tipo do indicador é obrigatório")
        TipoIndicador tipo,

        @Schema(description = "Valor medido, na unidade do indicador", example = "82.5")
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.0", message = "O valor não pode ser negativo")
        @Digits(integer = 10, fraction = 4, message = "O valor deve ter até 10 dígitos inteiros e 4 decimais")
        BigDecimal valor,

        @Schema(description = "Data de referência da medição", example = "2025-12-31")
        @NotNull(message = "A data de referência é obrigatória")
        @PastOrPresent(message = "A data de referência não pode estar no futuro")
        LocalDate dataReferencia,

        @Schema(description = "Fonte do dado", example = "Secretaria Municipal do Meio Ambiente")
        @Size(max = 200, message = "A fonte deve ter no máximo 200 caracteres")
        String fonte
) {
}
