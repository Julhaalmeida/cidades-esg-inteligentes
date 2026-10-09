package br.com.fiap.cidadesesg.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Cidade monitorada pela plataforma. O esquema da tabela é versionado pelo Flyway
 * (src/main/resources/db/migration).
 */
@Entity
@Table(name = "cidade")
public class Cidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false)
    private Integer populacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    /** Construtor exigido pela JPA. */
    protected Cidade() {
    }

    public Cidade(String nome, String uf, Integer populacao) {
        this.nome = nome;
        this.uf = uf;
        this.populacao = populacao;
    }

    public void atualizar(String nome, String uf, Integer populacao) {
        this.nome = nome;
        this.uf = uf;
        this.populacao = populacao;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUf() {
        return uf;
    }

    public Integer getPopulacao() {
        return populacao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
