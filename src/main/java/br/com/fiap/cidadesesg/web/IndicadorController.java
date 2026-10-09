package br.com.fiap.cidadesesg.web;

import br.com.fiap.cidadesesg.domain.TipoIndicador;
import br.com.fiap.cidadesesg.service.IndicadorService;
import br.com.fiap.cidadesesg.web.dto.IndicadorRequest;
import br.com.fiap.cidadesesg.web.dto.IndicadorResponse;
import br.com.fiap.cidadesesg.web.dto.TipoIndicadorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Indicadores ESG", description = "Catálogo de indicadores e medições por cidade")
public class IndicadorController {

    private final IndicadorService service;

    public IndicadorController(IndicadorService service) {
        this.service = service;
    }

    @GetMapping("/indicadores/tipos")
    @Operation(summary = "Lista os tipos de indicador aceitos e suas faixas de referência")
    public List<TipoIndicadorResponse> tipos() {
        return Arrays.stream(TipoIndicador.values())
                .map(TipoIndicadorResponse::de)
                .toList();
    }

    @GetMapping("/cidades/{cidadeId}/indicadores")
    @Operation(summary = "Lista as medições de uma cidade (mais recentes primeiro)")
    public List<IndicadorResponse> listar(@PathVariable("cidadeId") Long cidadeId) {
        return service.listarPorCidade(cidadeId);
    }

    @GetMapping("/cidades/{cidadeId}/indicadores/{indicadorId}")
    @Operation(summary = "Busca uma medição")
    public IndicadorResponse buscar(@PathVariable("cidadeId") Long cidadeId,
                                    @PathVariable("indicadorId") Long indicadorId) {
        return service.buscar(cidadeId, indicadorId);
    }

    @PostMapping("/cidades/{cidadeId}/indicadores")
    @Operation(summary = "Registra uma medição de indicador ESG para a cidade")
    public ResponseEntity<IndicadorResponse> registrar(@PathVariable("cidadeId") Long cidadeId,
                                                       @Valid @RequestBody IndicadorRequest request) {
        IndicadorResponse criado = service.registrar(cidadeId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @DeleteMapping("/cidades/{cidadeId}/indicadores/{indicadorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma medição")
    public void remover(@PathVariable("cidadeId") Long cidadeId,
                        @PathVariable("indicadorId") Long indicadorId) {
        service.remover(cidadeId, indicadorId);
    }
}
