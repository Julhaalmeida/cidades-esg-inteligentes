package br.com.fiap.cidadesesg.domain;

/**
 * Pilares ESG usados para agrupar os indicadores de uma cidade.
 */
public enum Dimensao {

    AMBIENTAL("Ambiental (E)"),
    SOCIAL("Social (S)"),
    GOVERNANCA("Governança (G)");

    private final String descricao;

    Dimensao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
