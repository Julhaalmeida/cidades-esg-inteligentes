package br.com.fiap.cidadesesg.web;

import br.com.fiap.cidadesesg.service.CidadeService;
import br.com.fiap.cidadesesg.web.dto.CidadeRequest;
import br.com.fiap.cidadesesg.web.dto.CidadeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cidades")
@Tag(name = "Cidades", description = "Cadastro das cidades monitoradas")
public class CidadeController {

    private final CidadeService service;

    public CidadeController(CidadeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as cidades", description = "Filtro opcional pela sigla da UF (ex.: ?uf=PR).")
    public List<CidadeResponse> listar(@RequestParam(name = "uf", required = false) String uf) {
        return service.listar(uf);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma cidade pelo id")
    public CidadeResponse buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma cidade")
    public ResponseEntity<CidadeResponse> criar(@Valid @RequestBody CidadeRequest request) {
        CidadeResponse criada = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma cidade")
    public CidadeResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody CidadeRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma cidade e todos os seus indicadores")
    public void remover(@PathVariable("id") Long id) {
        service.remover(id);
    }
}
