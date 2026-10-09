package br.com.fiap.cidadesesg.domain;

import br.com.fiap.cidadesesg.score.Medicao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Medição de um indicador ESG de uma cidade em uma data de referência.
 */
@Entity
@Table(name = "indicador_esg")
public class IndicadorEsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cidade_id", nullable = false)
    private Cidade cidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoIndicador tipo;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal valor;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(length = 200)
    private String fonte;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    /** Construtor exigido pela JPA. */
    protected IndicadorEsg() {
    }

    public IndicadorEsg(Cidade cidade, TipoIndicador tipo, BigDecimal valor, LocalDate dataReferencia, String fonte) {
        this.cidade = cidade;
        this.tipo = tipo;
        this.valor = valor;
        this.dataReferencia = dataReferencia;
        this.fonte = fonte;
    }

    @PrePersist
    void aoCriar() {
        this.criadoEm = Instant.now();
    }

    /** Converte a entidade para o objeto usado no cálculo do score. */
    public Medicao paraMedicao() {
        return new Medicao(tipo, valor.doubleValue(), dataReferencia);
    }

    public Long getId() {
        return id;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public TipoIndicador getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public String getFonte() {
        return fonte;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
