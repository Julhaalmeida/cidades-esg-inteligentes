package br.com.fiap.cidadesesg.score;

import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.domain.TipoIndicador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static br.com.fiap.cidadesesg.domain.TipoIndicador.COBERTURA_SANEAMENTO;
import static br.com.fiap.cidadesesg.domain.TipoIndicador.EMISSOES_CO2_PER_CAPITA;
import static br.com.fiap.cidadesesg.domain.TipoIndicador.ENERGIA_RENOVAVEL;
import static br.com.fiap.cidadesesg.domain.TipoIndicador.MORTALIDADE_INFANTIL;
import static br.com.fiap.cidadesesg.domain.TipoIndicador.RECICLAGEM_RESIDUOS;
import static br.com.fiap.cidadesesg.domain.TipoIndicador.TRANSPARENCIA_PUBLICA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cálculo do score ESG")
class EsgScoreCalculatorTest {

    private static final LocalDate DATA_2024 = LocalDate.of(2024, 12, 31);
    private static final LocalDate DATA_2025 = LocalDate.of(2025, 12, 31);

    private final EsgScoreCalculator calculadora = new EsgScoreCalculator(PesosEsg.padrao());

    private static Medicao medicao(TipoIndicador tipo, double valor) {
        return new Medicao(tipo, valor, DATA_2025);
    }

    @Nested
    @DisplayName("Normalização de 0 a 100")
    class Normalizacao {

        @Test
        @DisplayName("indicador em que maior é melhor é proporcional à faixa de referência")
        void maiorMelhor() {
            assertThat(EsgScoreCalculator.normalizar(ENERGIA_RENOVAVEL, 75)).isEqualTo(75.0);
            assertThat(EsgScoreCalculator.normalizar(RECICLAGEM_RESIDUOS, 25)).isEqualTo(50.0);
        }

        @Test
        @DisplayName("indicador em que menor é melhor tem a escala invertida")
        void menorMelhor() {
            assertThat(EsgScoreCalculator.normalizar(EMISSOES_CO2_PER_CAPITA, 2.5)).isEqualTo(75.0);
            assertThat(EsgScoreCalculator.normalizar(MORTALIDADE_INFANTIL, 30)).isEqualTo(0.0);
            assertThat(EsgScoreCalculator.normalizar(MORTALIDADE_INFANTIL, 0)).isEqualTo(100.0);
        }

        @ParameterizedTest(name = "{0} = {1} vira nota {2}")
        @CsvSource({
                "ENERGIA_RENOVAVEL,       150, 100.0",
                "RECICLAGEM_RESIDUOS,      80, 100.0",
                "EMISSOES_CO2_PER_CAPITA,  25,   0.0",
                "PARTICIPACAO_CIDADA,      -3,   0.0"
        })
        @DisplayName("valores fora da faixa ficam limitados entre 0 e 100")
        void limitaAosExtremos(TipoIndicador tipo, double valor, double notaEsperada) {
            assertThat(EsgScoreCalculator.normalizar(tipo, valor)).isEqualTo(notaEsperada);
        }
    }

    @Nested
    @DisplayName("Score geral")
    class ScoreGeral {

        @Test
        @DisplayName("sem medições, o resultado indica ausência de dados")
        void semMedicoes() {
            ResultadoScore resultado = calculadora.calcular(List.of());

            assertThat(resultado.possuiDados()).isFalse();
            assertThat(resultado.geral()).isNull();
            assertThat(resultado.classificacao()).isNull();
            assertThat(resultado.porDimensao()).isEmpty();
        }

        @Test
        @DisplayName("calcula a média de cada dimensão e a média ponderada geral")
        void mediaPonderada() {
            ResultadoScore resultado = calculadora.calcular(List.of(
                    medicao(ENERGIA_RENOVAVEL, 80),        // 80,0
                    medicao(EMISSOES_CO2_PER_CAPITA, 2),   // 80,0
                    medicao(RECICLAGEM_RESIDUOS, 20),      // 40,0  -> Ambiental 66,7
                    medicao(COBERTURA_SANEAMENTO, 90),     // 90,0
                    medicao(MORTALIDADE_INFANTIL, 6),      // 80,0  -> Social 85,0
                    medicao(TRANSPARENCIA_PUBLICA, 70)));  // 70,0  -> Governança 70,0

            assertThat(resultado.porDimensao())
                    .containsEntry(Dimensao.AMBIENTAL, 66.7)
                    .containsEntry(Dimensao.SOCIAL, 85.0)
                    .containsEntry(Dimensao.GOVERNANCA, 70.0);
            // 0,4 x 66,7 + 0,3 x 85,0 + 0,3 x 70,0 = 73,18
            assertThat(resultado.geral()).isEqualTo(73.2);
            assertThat(resultado.classificacao()).isEqualTo(Classificacao.B);
            assertThat(resultado.indicadoresConsiderados()).isEqualTo(6);
        }

        @Test
        @DisplayName("usa somente a medição mais recente de cada indicador")
        void medicaoMaisRecente() {
            ResultadoScore resultado = calculadora.calcular(List.of(
                    new Medicao(ENERGIA_RENOVAVEL, 40, DATA_2024),
                    new Medicao(ENERGIA_RENOVAVEL, 90, DATA_2025),
                    new Medicao(ENERGIA_RENOVAVEL, 10, DATA_2024)));

            assertThat(resultado.porDimensao()).containsEntry(Dimensao.AMBIENTAL, 90.0);
            assertThat(resultado.indicadoresConsiderados()).isEqualTo(1);
            assertThat(resultado.geral()).isEqualTo(90.0);
            assertThat(resultado.classificacao()).isEqualTo(Classificacao.A);
        }

        @Test
        @DisplayName("redistribui os pesos quando uma dimensão não tem dados")
        void redistribuiPesos() {
            ResultadoScore resultado = calculadora.calcular(List.of(
                    medicao(ENERGIA_RENOVAVEL, 50),       // Ambiental 50,0 (peso 0,4)
                    medicao(COBERTURA_SANEAMENTO, 100))); // Social 100,0 (peso 0,3)

            // (0,4 x 50 + 0,3 x 100) / 0,7 = 71,43
            assertThat(resultado.geral()).isEqualTo(71.4);
            assertThat(resultado.porDimensao()).doesNotContainKey(Dimensao.GOVERNANCA);
        }

        @Test
        @DisplayName("sem score geral quando só há dados em dimensões de peso zero")
        void somenteDimensaoSemPeso() {
            EsgScoreCalculator semGovernanca = new EsgScoreCalculator(new PesosEsg(0.5, 0.5, 0.0));

            ResultadoScore resultado = semGovernanca.calcular(List.of(medicao(TRANSPARENCIA_PUBLICA, 90)));

            assertThat(resultado.geral()).isNull();
            assertThat(resultado.porDimensao()).containsEntry(Dimensao.GOVERNANCA, 90.0);
        }
    }

    @Nested
    @DisplayName("Classificação e pesos")
    class ClassificacaoEPesos {

        @ParameterizedTest(name = "nota {0} = classe {1}")
        @CsvSource({"100, A", "80, A", "79.9, B", "60, B", "59.9, C", "40, C", "39.9, D", "0, D"})
        @DisplayName("classifica o score pelas faixas A, B, C e D")
        void faixas(double nota, Classificacao esperada) {
            assertThat(Classificacao.de(nota)).isEqualTo(esperada);
        }

        @Test
        @DisplayName("rejeita pesos negativos ou com soma zero")
        void pesosInvalidos() {
            assertThatThrownBy(() -> new PesosEsg(-0.1, 0.5, 0.6))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativos");
            assertThatThrownBy(() -> new PesosEsg(0, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maior que zero");
        }
    }
}
