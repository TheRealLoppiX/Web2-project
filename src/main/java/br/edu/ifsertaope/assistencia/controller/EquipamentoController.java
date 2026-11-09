package br.edu.ifsertaope.assistencia.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.edu.ifsertaope.assistencia.dto.EquipamentoRequest;
import br.edu.ifsertaope.assistencia.dto.EquipamentoResponse;
import br.edu.ifsertaope.assistencia.service.EquipamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipamentos")
@Tag(name = "Equipamentos", description = "Equipamentos trazidos pelos clientes")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    public EquipamentoController(EquipamentoService equipamentoService) {
        this.equipamentoService = equipamentoService;
    }

    @PostMapping
    @Operation(summary = "Cadastra equipamento vinculado a um cliente")
    public ResponseEntity<EquipamentoResponse> criar(@Valid @RequestBody EquipamentoRequest request) {
        EquipamentoResponse criado = equipamentoService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta equipamento por id")
    public EquipamentoResponse buscar(@PathVariable Long id) {
        return equipamentoService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza equipamento")
    public EquipamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EquipamentoRequest request) {
        return equipamentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove equipamento sem ordens de serviço")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        equipamentoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
