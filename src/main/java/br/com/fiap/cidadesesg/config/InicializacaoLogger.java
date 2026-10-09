package br.com.fiap.cidadesesg.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Registra no log, ao final da inicialização, qual ambiente e commit estão no ar.
 * Facilita conferir os deploys pelos logs do Render ou do Docker.
 */
@Component
public class InicializacaoLogger {

    private static final Logger log = LoggerFactory.getLogger(InicializacaoLogger.class);

    private final AppProperties propriedades;
    private final Environment environment;

    public InicializacaoLogger(AppProperties propriedades, Environment environment) {
        this.propriedades = propriedades;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aplicacaoPronta() {
        log.info("{} pronta | ambiente={} | commit={} | perfis={}",
                propriedades.nome(),
                propriedades.ambiente(),
                propriedades.commitCurto(),
                Arrays.toString(environment.getActiveProfiles()));
    }
}
