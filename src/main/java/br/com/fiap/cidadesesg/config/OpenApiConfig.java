package br.com.fiap.cidadesesg.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados exibidos no Swagger UI (/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cidadesEsgOpenApi(AppProperties propriedades, ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties build = buildProperties.getIfAvailable();
        String versao = build != null ? build.getVersion() : "dev";
        String descricao = """
                API REST para cadastro de cidades, registro de indicadores ESG \
                (Ambientais, Sociais e de Governança) e cálculo do score ESG com ranking.

                **Ambiente:** `%s` · **Commit:** `%s`""".formatted(propriedades.ambiente(), propriedades.commitCurto());
        return new OpenAPI().info(new Info()
                .title("Cidades ESG Inteligentes — API")
                .version(versao)
                .description(descricao)
                .license(new License().name("Uso acadêmico")));
    }
}
