package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.domain.Cidade;
import br.com.fiap.cidadesesg.exception.RecursoDuplicadoException;
import br.com.fiap.cidadesesg.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadesesg.repository.CidadeRepository;
import br.com.fiap.cidadesesg.web.dto.CidadeRequest;
import br.com.fiap.cidadesesg.web.dto.CidadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CidadeService {

    private final CidadeRepository repository;

    public CidadeService(CidadeRepository repository) {
        this.repository = repository;
    }

    public List<CidadeResponse> listar(String uf) {
        List<Cidade> cidades = (uf == null || uf.isBlank())
                ? repository.findAllByOrderByNomeAsc()
                : repository.findByUfIgnoreCaseOrderByNomeAsc(uf.trim());
        return cidades.stream().map(CidadeResponse::de).toList();
    }

    public CidadeResponse buscar(Long id) {
        return CidadeResponse.de(obter(id));
    }

    @Transactional
    public CidadeResponse criar(CidadeRequest request) {
        String nome = normalizarNome(request.nome());
        String uf = normalizarUf(request.uf());
        if (repository.existsByNomeIgnoreCaseAndUfIgnoreCase(nome, uf)) {
            throw new RecursoDuplicadoException(
                    "Já existe uma cidade chamada '%s' cadastrada em %s".formatted(nome, uf));
        }
        Cidade salva = repository.save(new Cidade(nome, uf, request.populacao()));
        return CidadeResponse.de(salva);
    }

    @Transactional
    public CidadeResponse atualizar(Long id, CidadeRequest request) {
        Cidade cidade = obter(id);
        String nome = normalizarNome(request.nome());
        String uf = normalizarUf(request.uf());
        if (repository.existsByNomeIgnoreCaseAndUfIgnoreCaseAndIdNot(nome, uf, id)) {
            throw new RecursoDuplicadoException(
                    "Já existe outra cidade chamada '%s' cadastrada em %s".formatted(nome, uf));
        }
        cidade.atualizar(nome, uf, request.populacao());
        // saveAndFlush dispara o @PreUpdate, então a resposta já traz o atualizadoEm novo
        return CidadeResponse.de(repository.saveAndFlush(cidade));
    }

    @Transactional
    public void remover(Long id) {
        // os indicadores da cidade são removidos pelo ON DELETE CASCADE do banco
        repository.delete(obter(id));
    }

    /** Busca a entidade ou lança 404. Usado também pelos outros serviços. */
    public Cidade obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade %d não encontrada".formatted(id)));
    }

    static String normalizarNome(String nome) {
        return nome.trim().replaceAll("\\s+", " ");
    }

    static String normalizarUf(String uf) {
        return uf.trim().toUpperCase(Locale.ROOT);
    }
}
