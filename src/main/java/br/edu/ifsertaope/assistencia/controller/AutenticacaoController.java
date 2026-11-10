package br.edu.ifsertaope.assistencia.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsertaope.assistencia.dto.LoginRequest;
import br.edu.ifsertaope.assistencia.dto.TokenResponse;
import br.edu.ifsertaope.assistencia.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login e emissão do token JWT")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica o usuário e devolve o token JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.login(request);
    }
}
