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

import br.edu.ifsertaope.assistencia.dto.ClienteRequest;
import br.edu.ifsertaope.assistencia.dto.ClienteResponse;
import br.edu.ifsertaope.assistencia.dto.EquipamentoResponse;
import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.service.ClienteService;
import br.edu.ifsertaope.assistencia.service.EquipamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro de clientes da assistência")
public class ClienteController {

    private final ClienteService clienteService;
    private final EquipamentoService equipamentoService;

    public ClienteController(ClienteService clienteService, EquipamentoService equipamentoService) {
        this.clienteService = clienteService;
        this.equipamentoService = equipamentoService;
    }

    @PostMapping
    @Operation(summary = "Cadastra cliente")
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse criado = clienteService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista clientes com paginação e filtro por nome parcial")
    public PaginaResponse<ClienteResponse> listar(@RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return clienteService.listar(nome, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta cliente por id")
    public ClienteResponse buscar(@PathVariable Long id) {
        return clienteService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove cliente sem equipamentos cadastrados")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        clienteService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/equipamentos")
    @Operation(summary = "Lista os equipamentos do cliente")
    public PaginaResponse<EquipamentoResponse> listarEquipamentos(@PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return equipamentoService.listarPorCliente(id, pageable);
    }
}
