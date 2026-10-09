package br.com.fiap.cidadesesg.score;

import br.com.fiap.cidadesesg.domain.Dimensao;

/**
 * Peso de cada pilar ESG no score geral. Os pesos não precisam somar 1:
 * o cálculo usa média ponderada e redistribui o peso das dimensões sem dados.
 */
public record PesosEsg(double ambiental, double social, double governanca) {

    public PesosEsg {
        if (!Double.isFinite(ambiental) || !Double.isFinite(social) || !Double.isFinite(governanca)) {
            throw new IllegalArgumentException("Os pesos ESG devem ser números válidos");
        }
        if (ambiental < 0 || social < 0 || governanca < 0) {
            throw new IllegalArgumentException("Os pesos ESG não podem ser negativos");
        }
        if (ambiental + social + governanca <= 0) {
            throw new IllegalArgumentException("A soma dos pesos ESG deve ser maior que zero");
        }
    }

    /** Pesos padrão: 40% ambiental, 30% social e 30% governança. */
    public static PesosEsg padrao() {
        return new PesosEsg(0.4, 0.3, 0.3);
    }

    public double de(Dimensao dimensao) {
        return switch (dimensao) {
            case AMBIENTAL -> ambiental;
            case SOCIAL -> social;
            case GOVERNANCA -> governanca;
        };
    }
}
