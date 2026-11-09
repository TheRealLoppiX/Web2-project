package br.edu.ifsertaope.assistencia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsertaope.assistencia.model.OrdemServico;
import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    @Override
    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByStatus(StatusOrdemServico status, Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByEquipamentoClienteId(Long clienteId, Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByTecnicoId(Long tecnicoId, Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByStatusAndEquipamentoClienteId(StatusOrdemServico status, Long clienteId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByStatusAndTecnicoId(StatusOrdemServico status, Long tecnicoId, Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByEquipamentoClienteIdAndTecnicoId(Long clienteId, Long tecnicoId, Pageable pageable);

    @EntityGraph(attributePaths = {"equipamento", "equipamento.cliente", "tecnico"})
    Page<OrdemServico> findByStatusAndEquipamentoClienteIdAndTecnicoId(StatusOrdemServico status, Long clienteId,
            Long tecnicoId, Pageable pageable);

    boolean existsByEquipamentoId(Long equipamentoId);
}
