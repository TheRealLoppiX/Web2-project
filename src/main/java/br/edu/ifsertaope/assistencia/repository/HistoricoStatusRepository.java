package br.edu.ifsertaope.assistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsertaope.assistencia.model.HistoricoStatus;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    List<HistoricoStatus> findByOrdemServicoIdOrderByDataHoraAscIdAsc(Long ordemServicoId);
}
