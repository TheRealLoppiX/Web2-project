package br.edu.ifsertaope.assistencia.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.dto.UsuarioRequest;
import br.edu.ifsertaope.assistencia.dto.UsuarioResponse;
import br.edu.ifsertaope.assistencia.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Contas de acesso da equipe (somente gerente)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra usuário com perfil ATENDENTE, TECNICO ou GERENTE")
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista usuários com paginação")
    public PaginaResponse<UsuarioResponse> listar(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return usuarioService.listar(pageable);
    }
}
