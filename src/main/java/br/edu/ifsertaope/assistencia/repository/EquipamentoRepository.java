package br.edu.ifsertaope.assistencia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsertaope.assistencia.model.Equipamento;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    @EntityGraph(attributePaths = "cliente")
    Page<Equipamento> findByClienteId(Long clienteId, Pageable pageable);

    boolean existsByClienteId(Long clienteId);
}
