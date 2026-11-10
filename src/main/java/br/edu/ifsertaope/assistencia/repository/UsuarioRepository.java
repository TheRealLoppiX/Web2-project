package br.edu.ifsertaope.assistencia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsertaope.assistencia.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCase(String login);
}
