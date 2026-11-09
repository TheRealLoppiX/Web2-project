package br.edu.ifsertaope.assistencia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsertaope.assistencia.model.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    Page<Tecnico> findByAtivo(boolean ativo, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
