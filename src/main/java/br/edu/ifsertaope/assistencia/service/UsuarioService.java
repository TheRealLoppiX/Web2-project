package br.edu.ifsertaope.assistencia.service;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.dto.UsuarioRequest;
import br.edu.ifsertaope.assistencia.dto.UsuarioResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.mapper.UsuarioMapper;
import br.edu.ifsertaope.assistencia.model.Perfil;
import br.edu.ifsertaope.assistencia.model.Usuario;
import br.edu.ifsertaope.assistencia.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        return UsuarioMapper.paraResponse(cadastrar(request.nome(), request.login(), request.senha(), request.perfil()));
    }

    @Transactional
    public boolean criarGerenteInicialSeNecessario(String nome, String login, String senha) {
        if (usuarioRepository.count() > 0) {
            return false;
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalStateException(
                    "Nenhum usuário cadastrado: defina ADMIN_PASSWORD para criar o gerente inicial.");
        }
        cadastrar(nome, login, senha, Perfil.GERENTE);
        return true;
    }

    public PaginaResponse<UsuarioResponse> listar(Pageable pageable) {
        return PaginaResponse.de(usuarioRepository.findAll(pageable), UsuarioMapper::paraResponse);
    }

    private Usuario cadastrar(String nome, String login, String senha, Perfil perfil) {
        if (usuarioRepository.existsByLoginIgnoreCase(UsuarioMapper.normalizarLogin(login))) {
            throw new ConflitoException("Já existe um usuário com este login.");
        }
        Usuario usuario = UsuarioMapper.paraEntidade(nome, login, passwordEncoder.encode(senha), perfil);
        return usuarioRepository.save(usuario);
    }
}
