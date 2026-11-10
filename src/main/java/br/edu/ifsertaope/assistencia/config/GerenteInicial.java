package br.edu.ifsertaope.assistencia.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.edu.ifsertaope.assistencia.service.UsuarioService;

@Component
public class GerenteInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GerenteInicial.class);

    private final UsuarioService usuarioService;
    private final SegurancaProperties propriedades;

    public GerenteInicial(UsuarioService usuarioService, SegurancaProperties propriedades) {
        this.usuarioService = usuarioService;
        this.propriedades = propriedades;
    }

    @Override
    public void run(ApplicationArguments args) {
        SegurancaProperties.Administrador administrador = propriedades.administrador();
        if (usuarioService.criarGerenteInicialSeNecessario(administrador.nome(), administrador.login(),
                administrador.senha())) {
            LOG.info("Usuário gerente inicial '{}' criado.", administrador.login());
        }
    }
}
