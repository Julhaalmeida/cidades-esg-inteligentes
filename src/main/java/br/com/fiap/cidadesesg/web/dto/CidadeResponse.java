package br.com.fiap.cidadesesg.web.dto;

import br.com.fiap.cidadesesg.domain.Cidade;

import java.time.Instant;

public record CidadeResponse(
        Long id,
        String nome,
        String uf,
        Integer populacao,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static CidadeResponse de(Cidade cidade) {
        return new CidadeResponse(
                cidade.getId(),
                cidade.getNome(),
                cidade.getUf(),
                cidade.getPopulacao(),
                cidade.getCriadoEm(),
                cidade.getAtualizadoEm());
    }
}
