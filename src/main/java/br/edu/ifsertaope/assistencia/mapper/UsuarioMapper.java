package br.edu.ifsertaope.assistencia.mapper;

import java.util.Locale;

import br.edu.ifsertaope.assistencia.dto.UsuarioResponse;
import br.edu.ifsertaope.assistencia.model.Perfil;
import br.edu.ifsertaope.assistencia.model.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario paraEntidade(String nome, String login, String senhaHash, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setLogin(normalizarLogin(login));
        usuario.setSenhaHash(senhaHash);
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuario;
    }

    public static String normalizarLogin(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }

    public static UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.isAtivo());
    }
}
