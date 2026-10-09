package br.com.fiap.cidadesesg.score;

/**
 * Faixas de classificação do score ESG geral (0 a 100).
 */
public enum Classificacao {

    A("Excelente", 80.0),
    B("Bom", 60.0),
    C("Regular", 40.0),
    D("Crítico", 0.0);

    private final String descricao;
    private final double notaMinima;

    Classificacao(String descricao, double notaMinima) {
        this.descricao = descricao;
        this.notaMinima = notaMinima;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getNotaMinima() {
        return notaMinima;
    }

    /** Retorna a primeira faixa cuja nota mínima é atingida pelo score informado. */
    public static Classificacao de(double score) {
        for (Classificacao classificacao : values()) {
            if (score >= classificacao.notaMinima) {
                return classificacao;
            }
        }
        return D;
    }
}
