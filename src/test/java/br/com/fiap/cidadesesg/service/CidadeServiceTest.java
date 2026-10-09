package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.exception.RecursoDuplicadoException;
import br.com.fiap.cidadesesg.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadesesg.repository.CidadeRepository;
import br.com.fiap.cidadesesg.web.dto.CidadeRequest;
import br.com.fiap.cidadesesg.web.dto.CidadeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de cidades")
class CidadeServiceTest {

    @Mock
    private CidadeRepository repository;

    @InjectMocks
    private CidadeService service;

    @Test
    @DisplayName("cadastra a cidade normalizando espaços no nome e a UF em maiúsculas")
    void criaNormalizandoDados() {
        given(repository.existsByNomeIgnoreCaseAndUfIgnoreCase("Porto Alegre", "RS")).willReturn(false);
        given(repository.save(any(Cidade.class))).willAnswer(invocacao -> invocacao.getArgument(0));

        CidadeResponse criada = service.criar(new CidadeRequest("  Porto   Alegre ", "rs", 1_332_000));

        assertThat(criada.nome()).isEqualTo("Porto Alegre");
        assertThat(criada.uf()).isEqualTo("RS");
        assertThat(criada.populacao()).isEqualTo(1_332_000);
    }

    @Test
    @DisplayName("não permite cadastrar a mesma cidade duas vezes na mesma UF")
    void naoPermiteDuplicada() {
        given(repository.existsByNomeIgnoreCaseAndUfIgnoreCase("Curitiba", "PR")).willReturn(true);

        assertThatThrownBy(() -> service.criar(new CidadeRequest("Curitiba", "PR", 1_773_000)))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("Curitiba");
        verify(repository, never()).save(any(Cidade.class));
    }

    @Test
    @DisplayName("lança 404 ao buscar uma cidade inexistente")
    void buscaInexistente() {
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cidade 99 não encontrada");
    }

    @Test
    @DisplayName("atualiza os dados de uma cidade existente")
    void atualiza() {
        Cidade cidade = new Cidade("Recife", "PE", 1_488_000);
        given(repository.findById(5L)).willReturn(Optional.of(cidade));
        given(repository.existsByNomeIgnoreCaseAndUfIgnoreCaseAndIdNot("Recife", "PE", 5L)).willReturn(false);
        given(repository.saveAndFlush(cidade)).willReturn(cidade);

        CidadeResponse atualizada = service.atualizar(5L, new CidadeRequest("Recife", "pe", 1_500_000));

        assertThat(atualizada.populacao()).isEqualTo(1_500_000);
        assertThat(atualizada.uf()).isEqualTo("PE");
    }

    @Test
    @DisplayName("filtra a listagem pela UF quando informada")
    void listaPorUf() {
        given(repository.findByUfIgnoreCaseOrderByNomeAsc("SC"))
                .willReturn(List.of(new Cidade("Florianópolis", "SC", 537_000)));

        List<CidadeResponse> cidades = service.listar(" SC ");

        assertThat(cidades).extracting(CidadeResponse::nome).containsExactly("Florianópolis");
    }
}
