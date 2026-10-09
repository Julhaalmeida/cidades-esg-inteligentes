package br.com.fiap.cidadesesg.score;

import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.domain.TipoIndicador;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Calcula o score ESG de uma cidade a partir das medições dos seus indicadores.
 *
 * <ol>
 *   <li>Para cada tipo de indicador, considera apenas a medição mais recente.</li>
 *   <li>Normaliza cada valor para uma nota de 0 a 100, conforme a faixa de referência do indicador.</li>
 *   <li>Calcula a nota de cada dimensão (E, S e G) como a média das notas dos seus indicadores.</li>
 *   <li>Calcula o score geral como a média ponderada das dimensões que possuem dados.</li>
 * </ol>
 *
 * <p>Classe de domínio pura (sem Spring), o que facilita os testes unitários.</p>
 */
public class EsgScoreCalculator {

    private final PesosEsg pesos;

    public EsgScoreCalculator(PesosEsg pesos) {
        this.pesos = Objects.requireNonNull(pesos, "pesos");
    }

    public PesosEsg getPesos() {
        return pesos;
    }

    /**
     * Converte o valor bruto de um indicador em uma nota de 0 a 100 (uma casa decimal).
     * Valores fora da faixa de referência são limitados aos extremos da escala.
     */
    public static double normalizar(TipoIndicador tipo, double valor) {
        double proporcao = (valor - tipo.getMinimo()) / (tipo.getMaximo() - tipo.getMinimo());
        if (tipo.isMenorMelhor()) {
            proporcao = 1.0 - proporcao;
        }
        double limitada = Math.max(0.0, Math.min(1.0, proporcao));
        return arredondar(limitada * 100.0);
    }

    public ResultadoScore calcular(Collection<Medicao> medicoes) {
        if (medicoes == null || medicoes.isEmpty()) {
            return ResultadoScore.semDados();
        }

        // 1) Medição mais recente de cada indicador (em caso de empate, mantém a primeira recebida)
        Map<TipoIndicador, Medicao> maisRecentes = new EnumMap<>(TipoIndicador.class);
        for (Medicao medicao : medicoes) {
            maisRecentes.merge(medicao.tipo(), medicao,
                    (atual, nova) -> nova.dataReferencia().isAfter(atual.dataReferencia()) ? nova : atual);
        }

        // 2 e 3) Nota de cada dimensão = média das notas normalizadas dos seus indicadores
        Map<Dimensao, Double> porDimensao = new EnumMap<>(Dimensao.class);
        for (Dimensao dimensao : Dimensao.values()) {
            maisRecentes.values().stream()
                    .filter(medicao -> medicao.tipo().getDimensao() == dimensao)
                    .mapToDouble(medicao -> normalizar(medicao.tipo(), medicao.valor()))
                    .average()
                    .ifPresent(media -> porDimensao.put(dimensao, arredondar(media)));
        }

        // 4) Score geral = média ponderada das dimensões com dados
        double somaPonderada = 0.0;
        double somaPesos = 0.0;
        for (Map.Entry<Dimensao, Double> nota : porDimensao.entrySet()) {
            double peso = pesos.de(nota.getKey());
            somaPonderada += nota.getValue() * peso;
            somaPesos += peso;
        }
        if (somaPesos == 0.0) {
            return new ResultadoScore(null, null, porDimensao, maisRecentes.size());
        }

        double geral = arredondar(somaPonderada / somaPesos);
        return new ResultadoScore(geral, Classificacao.de(geral), porDimensao, maisRecentes.size());
    }

    static double arredondar(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
