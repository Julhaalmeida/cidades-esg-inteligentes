package br.com.fiap.cidadesesg.repository;

import br.com.fiap.cidadesesg.domain.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {

    List<Cidade> findAllByOrderByNomeAsc();

    List<Cidade> findByUfIgnoreCaseOrderByNomeAsc(String uf);

    boolean existsByNomeIgnoreCaseAndUfIgnoreCase(String nome, String uf);

    boolean existsByNomeIgnoreCaseAndUfIgnoreCaseAndIdNot(String nome, String uf, Long id);
}
