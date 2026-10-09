package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.domain.IndicadorEsg;
import br.com.fiap.cidadesesg.domain.TipoIndicador;
import br.com.fiap.cidadesesg.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadesesg.exception.RegraDeNegocioException;
import br.com.fiap.cidadesesg.repository.IndicadorEsgRepository;
import br.com.fiap.cidadesesg.web.dto.IndicadorRequest;
import br.com.fiap.cidadesesg.web.dto.IndicadorResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class IndicadorService {

    private static final BigDecimal CEM_POR_CENTO = BigDecimal.valueOf(100);

    private final IndicadorEsgRepository repository;
    private final CidadeService cidadeService;

    public IndicadorService(IndicadorEsgRepository repository, CidadeService cidadeService) {
        this.repository = repository;
        this.cidadeService = cidadeService;
    }

    public List<IndicadorResponse> listarPorCidade(Long cidadeId) {
        cidadeService.obter(cidadeId); // 404 se a cidade não existir
        return repository.findByCidadeIdOrderByDataReferenciaDescIdDesc(cidadeId).stream()
                .map(IndicadorResponse::de)
                .toList();
    }

    public IndicadorResponse buscar(Long cidadeId, Long indicadorId) {
        return IndicadorResponse.de(obter(cidadeId, indicadorId));
    }

    @Transactional
    public IndicadorResponse registrar(Long cidadeId, IndicadorRequest request) {
        Cidade cidade = cidadeService.obter(cidadeId);
        TipoIndicador tipo = request.tipo();
        if (tipo.isPercentual() && request.valor().compareTo(CEM_POR_CENTO) > 0) {
            throw new RegraDeNegocioException(
                    "O indicador %s é percentual e não pode passar de 100".formatted(tipo.name()));
        }
        IndicadorEsg indicador = new IndicadorEsg(
                cidade, tipo, request.valor(), request.dataReferencia(), normalizarFonte(request.fonte()));
        return IndicadorResponse.de(repository.save(indicador));
    }

    @Transactional
    public void remover(Long cidadeId, Long indicadorId) {
        repository.delete(obter(cidadeId, indicadorId));
    }

    private IndicadorEsg obter(Long cidadeId, Long indicadorId) {
        return repository.findByIdAndCidadeId(indicadorId, cidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Indicador %d não encontrado na cidade %d".formatted(indicadorId, cidadeId)));
    }

    private static String normalizarFonte(String fonte) {
        return (fonte == null || fonte.isBlank()) ? null : fonte.trim();
    }
}
