package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.domain.IndicadorEsg;
import br.com.fiap.cidadesesg.domain.TipoIndicador;
import br.com.fiap.cidadesesg.repository.CidadeRepository;
import br.com.fiap.cidadesesg.repository.IndicadorEsgRepository;
import br.com.fiap.cidadesesg.score.EsgScoreCalculator;
import br.com.fiap.cidadesesg.score.PesosEsg;
import br.com.fiap.cidadesesg.web.dto.RankingItemResponse;
import br.com.fiap.cidadesesg.web.dto.ScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de score e ranking")
class ScoreServiceTest {

    @Mock
    private CidadeService cidadeService;

    @Mock
    private CidadeRepository cidadeRepository;

    @Mock
    private IndicadorEsgRepository indicadorRepository;

    private ScoreService service;

    @BeforeEach
    void configurar() {
        service = new ScoreService(cidadeService, cidadeRepository, indicadorRepository,
                new EsgScoreCalculator(PesosEsg.padrao()));
    }

    private static Cidade cidade(long id, String nome, String uf) {
        Cidade cidade = new Cidade(nome, uf, 100_000);
        ReflectionTestUtils.setField(cidade, "id", id);
        return cidade;
    }

    private static IndicadorEsg indicador(Cidade cidade, TipoIndicador tipo, String valor) {
        return new IndicadorEsg(cidade, tipo, new BigDecimal(valor), LocalDate.of(2025, 12, 31), "teste");
    }

    @Test
    @DisplayName("ordena o ranking do maior para o menor score, com cidades sem dados no fim")
    void ordenaRanking() {
        Cidade alfa = cidade(1L, "Alfa", "SP");
        Cidade beta = cidade(2L, "Beta", "RJ");
        Cidade gama = cidade(3L, "Gama", "MG");
        given(cidadeRepository.findAll()).willReturn(List.of(alfa, beta, gama));
        given(indicadorRepository.findAllByOrderByDataReferenciaDescIdDesc()).willReturn(List.of(
                indicador(alfa, TipoIndicador.ENERGIA_RENOVAVEL, "40"),
                indicador(beta, TipoIndicador.ENERGIA_RENOVAVEL, "90")));

        List<RankingItemResponse> ranking = service.ranking();

        assertThat(ranking).extracting(RankingItemResponse::cidade).containsExactly("Beta", "Alfa", "Gama");
        assertThat(ranking).extracting(RankingItemResponse::posicao).containsExactly(1, 2, 3);
        assertThat(ranking.get(0).classificacao()).isEqualTo("A");
        assertThat(ranking.get(2).scoreGeral()).isNull();
        assertThat(ranking.get(2).descricaoClassificacao()).isEqualTo("Sem dados suficientes");
    }

    @Test
    @DisplayName("calcula o score de uma cidade com as notas por dimensão e os pesos usados")
    void calculaScoreDaCidade() {
        Cidade alfa = cidade(1L, "Alfa", "SP");
        given(cidadeService.obter(1L)).willReturn(alfa);
        given(indicadorRepository.findByCidadeIdOrderByDataReferenciaDescIdDesc(1L)).willReturn(List.of(
                indicador(alfa, TipoIndicador.COBERTURA_SANEAMENTO, "64")));

        ScoreResponse score = service.calcular(1L);

        assertThat(score.scoreGeral()).isEqualTo(64.0);
        assertThat(score.classificacao()).isEqualTo("B");
        assertThat(score.dimensoes()).containsKeys(Dimensao.AMBIENTAL, Dimensao.SOCIAL, Dimensao.GOVERNANCA);
        assertThat(score.dimensoes().get(Dimensao.AMBIENTAL)).isNull();
        assertThat(score.pesos().get(Dimensao.SOCIAL)).isEqualTo(0.3);
    }
}
