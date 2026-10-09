package br.com.fiap.cidadesesg.config;

import br.com.fiap.cidadesesg.score.EsgScoreCalculator;
import br.com.fiap.cidadesesg.score.PesosEsg;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsgConfig {

    @Bean
    public EsgScoreCalculator esgScoreCalculator(AppProperties propriedades) {
        AppProperties.Pesos pesos = propriedades.pesos();
        PesosEsg pesosEsg = (pesos == null)
                ? PesosEsg.padrao()
                : new PesosEsg(pesos.ambiental(), pesos.social(), pesos.governanca());
        return new EsgScoreCalculator(pesosEsg);
    }
}
