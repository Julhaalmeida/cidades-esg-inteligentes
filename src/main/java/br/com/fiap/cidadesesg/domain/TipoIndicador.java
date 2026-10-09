package br.com.fiap.cidadesesg.domain;

/**
 * Catálogo de indicadores ESG aceitos pela plataforma.
 *
 * <p>Cada indicador define a faixa de referência usada para normalizar o valor
 * medido em uma nota de 0 a 100: {@code minimo} e {@code maximo} são os extremos
 * da escala e {@code menorMelhor} indica se valores menores são desejáveis
 * (ex.: emissões de CO₂ e mortalidade infantil).</p>
 */
public enum TipoIndicador {

    // ----- Ambiental -----
    EMISSOES_CO2_PER_CAPITA(Dimensao.AMBIENTAL, "Emissões de CO₂ per capita", "tCO₂e/hab/ano", 0, 10, true),
    ENERGIA_RENOVAVEL(Dimensao.AMBIENTAL, "Energia elétrica de fontes renováveis", "%", 0, 100, false),
    RECICLAGEM_RESIDUOS(Dimensao.AMBIENTAL, "Resíduos sólidos urbanos reciclados", "%", 0, 50, false),

    // ----- Social -----
    COBERTURA_SANEAMENTO(Dimensao.SOCIAL, "População atendida por coleta de esgoto", "%", 0, 100, false),
    ACESSO_TRANSPORTE_PUBLICO(Dimensao.SOCIAL, "População a até 500 m de transporte público", "%", 0, 100, false),
    MORTALIDADE_INFANTIL(Dimensao.SOCIAL, "Mortalidade infantil", "óbitos por mil nascidos vivos", 0, 30, true),

    // ----- Governança -----
    TRANSPARENCIA_PUBLICA(Dimensao.GOVERNANCA, "Índice de transparência pública", "pontos (0 a 100)", 0, 100, false),
    SERVICOS_DIGITAIS(Dimensao.GOVERNANCA, "Serviços públicos disponíveis on-line", "%", 0, 100, false),
    PARTICIPACAO_CIDADA(Dimensao.GOVERNANCA, "População que participa de consultas públicas", "%", 0, 10, false);

    private final Dimensao dimensao;
    private final String descricao;
    private final String unidade;
    private final double minimo;
    private final double maximo;
    private final boolean menorMelhor;

    TipoIndicador(Dimensao dimensao, String descricao, String unidade,
                  double minimo, double maximo, boolean menorMelhor) {
        this.dimensao = dimensao;
        this.descricao = descricao;
        this.unidade = unidade;
        this.minimo = minimo;
        this.maximo = maximo;
        this.menorMelhor = menorMelhor;
    }

    public Dimensao getDimensao() {
        return dimensao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    public boolean isMenorMelhor() {
        return menorMelhor;
    }

    /** Indicadores medidos em percentual não podem passar de 100. */
    public boolean isPercentual() {
        return "%".equals(unidade);
    }
}
