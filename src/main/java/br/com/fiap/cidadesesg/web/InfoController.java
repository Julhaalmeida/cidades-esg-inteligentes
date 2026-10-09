package br.com.fiap.cidadesesg.web;

import br.com.fiap.cidadesesg.config.AppProperties;
import br.com.fiap.cidadesesg.web.dto.InfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/info")
@Tag(name = "Aplicação", description = "Identificação do ambiente e da versão em execução")
public class InfoController {

    private final AppProperties propriedades;
    private final String versao;
    private final Instant inicializadoEm = Instant.now();

    public InfoController(AppProperties propriedades, ObjectProvider<BuildProperties> buildProperties) {
        this.propriedades = propriedades;
        BuildProperties build = buildProperties.getIfAvailable();
        this.versao = build != null ? build.getVersion() : "dev";
    }

    @GetMapping
    @Operation(summary = "Mostra ambiente, versão e commit em execução",
            description = "Usado pelo pipeline CI/CD para confirmar que o deploy publicou o commit esperado.")
    public InfoResponse info() {
        return new InfoResponse(
                propriedades.nome(),
                versao,
                propriedades.ambiente(),
                propriedades.commitCurto(),
                inicializadoEm,
                Runtime.version().toString());
    }
}
