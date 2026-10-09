package br.com.fiap.cidadesesg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades da aplicação (prefixo {@code app} no application.yml).
 * Os valores vêm de variáveis de ambiente, o que permite usar a mesma imagem
 * Docker em todos os ambientes (local, staging e produção).
 *
 * @param nome     nome exibido na API e na página inicial
 * @param ambiente ambiente atual (APP_ENV): local, staging ou production
 * @param commit   commit em execução (RENDER_GIT_COMMIT no Render ou APP_COMMIT na imagem)
 * @param pesos    peso de cada pilar ESG no score geral
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String nome, String ambiente, String commit, Pesos pesos) {

    public record Pesos(double ambiental, double social, double governanca) {
    }

    /** Commit abreviado (7 caracteres), como o GitHub exibe. */
    public String commitCurto() {
        if (commit == null || commit.isBlank()) {
            return "desconhecido";
        }
        return commit.length() > 7 ? commit.substring(0, 7) : commit;
    }
}
