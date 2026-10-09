package br.com.fiap.cidadesesg.repository;

import br.com.fiap.cidadesesg.domain.IndicadorEsg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IndicadorEsgRepository extends JpaRepository<IndicadorEsg, Long> {

    /** Medições de uma cidade, da mais recente para a mais antiga. */
    List<IndicadorEsg> findByCidadeIdOrderByDataReferenciaDescIdDesc(Long cidadeId);

    Optional<IndicadorEsg> findByIdAndCidadeId(Long id, Long cidadeId);

    /** Todas as medições, da mais recente para a mais antiga (usado no ranking). */
    List<IndicadorEsg> findAllByOrderByDataReferenciaDescIdDesc();
}
