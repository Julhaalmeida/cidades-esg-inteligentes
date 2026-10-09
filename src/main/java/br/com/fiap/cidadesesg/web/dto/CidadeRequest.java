package br.com.fiap.cidadesesg.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados para cadastrar ou atualizar uma cidade. */
public record CidadeRequest(

        @Schema(description = "Nome da cidade", example = "Porto Alegre")
        @NotBlank(message = "O nome da cidade é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Schema(description = "Sigla da unidade federativa", example = "RS")
        @NotBlank(message = "A UF é obrigatória")
        @Pattern(regexp = CidadeRequest.UFS_VALIDAS, flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "UF inválida: informe a sigla de um estado brasileiro (ex.: SP)")
        String uf,

        @Schema(description = "População estimada", example = "1332000")
        @NotNull(message = "A população é obrigatória")
        @Positive(message = "A população deve ser maior que zero")
        Integer populacao
) {

    static final String UFS_VALIDAS =
            "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO";
}
