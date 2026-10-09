package br.com.fiap.cidadesesg.web.dto;

import java.time.Instant;

/**
 * Identificação da instância em execução. É usado pelo pipeline para confirmar
 * que o deploy publicou o commit esperado em cada ambiente.
 */
public record InfoResponse(
        String aplicacao,
        String versao,
        String ambiente,
        String commit,
        Instant inicializadoEm,
        String java
) {
}
