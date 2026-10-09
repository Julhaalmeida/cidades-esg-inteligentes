package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.domain.Dimensao;
import br.com.fiap.cidadesesg.domain.IndicadorEsg;
import br.com.fiap.cidadesesg.domain.TipoIndicador;
import br.com.fiap.cidadesesg.exception.RegraDeNegocioException;
import br.com.fiap.cidadesesg.repository.IndicadorEsgRepository;
import br.com.fiap.cidadesesg.web.dto.IndicadorRequest;
import br.com.fiap.cidadesesg.web.dto.IndicadorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de indicadores")
class IndicadorServiceTest {

    @Mock
    private IndicadorEsgRepository repository;

    @Mock
    private CidadeService cidadeService;

    @InjectMocks
    private IndicadorService service;

    private final Cidade cidade = new Cidade("Curitiba", "PR", 1_773_000);

    @Test
    @DisplayName("registra a medição e devolve a nota normalizada")
    void registraMedicao() {
        given(cidadeService.obter(1L)).willReturn(cidade);
        given(repository.save(any(IndicadorEsg.class))).willAnswer(invocacao -> invocacao.getArgument(0));
        IndicadorRequest request = new IndicadorRequest(
                TipoIndicador.EMISSOES_CO2_PER_CAPITA, new BigDecimal("2.5"), LocalDate.of(2025, 12, 31), "  IBGE  ");

        IndicadorResponse resposta = service.registrar(1L, request);

        assertThat(resposta.dimensao()).isEqualTo(Dimensao.AMBIENTAL);
        assertThat(resposta.notaNormalizada()).isEqualTo(75.0);
        assertThat(resposta.fonte()).isEqualTo("IBGE");
    }

    @Test
    @DisplayName("rejeita indicador percentual acima de 100")
    void rejeitaPercentualAcimaDeCem() {
        given(cidadeService.obter(1L)).willReturn(cidade);
        IndicadorRequest request = new IndicadorRequest(
                TipoIndicador.ENERGIA_RENOVAVEL, new BigDecimal("120"), LocalDate.of(2025, 12, 31), null);

        assertThatThrownBy(() -> service.registrar(1L, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("ENERGIA_RENOVAVEL");
        verify(repository, never()).save(any(IndicadorEsg.class));
    }
}
