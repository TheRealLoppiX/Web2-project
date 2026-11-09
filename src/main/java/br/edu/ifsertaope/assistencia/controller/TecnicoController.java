package br.edu.ifsertaope.assistencia.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.dto.TecnicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoResponse;
import br.edu.ifsertaope.assistencia.service.TecnicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tecnicos")
@Tag(name = "Técnicos", description = "Equipe técnica da assistência")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @PostMapping
    @Operation(summary = "Cadastra técnico como ativo")
    public ResponseEntity<TecnicoResponse> criar(@Valid @RequestBody TecnicoRequest request) {
        TecnicoResponse criado = tecnicoService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista técnicos com paginação e filtro opcional por ativos")
    public PaginaResponse<TecnicoResponse> listar(@RequestParam(required = false) Boolean ativos,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return tecnicoService.listar(ativos, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta técnico por id")
    public TecnicoResponse buscar(@PathVariable Long id) {
        return tecnicoService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza técnico")
    public TecnicoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TecnicoAtualizacaoRequest request) {
        return tecnicoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa técnico (exclusão lógica)")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        tecnicoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
