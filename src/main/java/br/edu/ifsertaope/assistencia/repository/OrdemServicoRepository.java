package br.edu.ifsertaope.assistencia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.edu.ifsertaope.assistencia.model.OrdemServico;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long>,
        JpaSpecificationExecutor<OrdemServico> {

    @Override
    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findAll(Specification<OrdemServico> specification, Pageable pageable);

    boolean existsByEquipamentoId(Long equipamentoId);
}
