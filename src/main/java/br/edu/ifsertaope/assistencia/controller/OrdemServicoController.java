package br.edu.ifsertaope.assistencia.controller;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.edu.ifsertaope.assistencia.dto.AlteracaoStatusRequest;
import br.edu.ifsertaope.assistencia.dto.AtribuicaoTecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.HistoricoStatusResponse;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoAberturaRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoResponse;
import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;
import br.edu.ifsertaope.assistencia.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ordens-servico")
@Tag(name = "Ordens de serviço", description = "Abertura, acompanhamento e encerramento de OS")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PostMapping
    @Operation(summary = "Abre OS em AGUARDANDO_ANALISE")
    public ResponseEntity<OrdemServicoResponse> abrir(@Valid @RequestBody OrdemServicoAberturaRequest request) {
        OrdemServicoResponse criada = ordemServicoService.abrir(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @GetMapping
    @Operation(summary = "Lista OS com paginação e filtros por status, cliente e técnico")
    public PaginaResponse<OrdemServicoResponse> listar(
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long tecnicoId,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ordemServicoService.listar(status, clienteId, tecnicoId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta OS por id")
    public OrdemServicoResponse buscar(@PathVariable Long id) {
        return ordemServicoService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Registra laudo técnico, serviços executados e valor total")
    public OrdemServicoResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody OrdemServicoAtualizacaoRequest request) {
        return ordemServicoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/tecnico")
    @Operation(summary = "Atribui ou troca o técnico responsável")
    public OrdemServicoResponse atribuirTecnico(@PathVariable Long id,
            @Valid @RequestBody AtribuicaoTecnicoRequest request) {
        return ordemServicoService.atribuirTecnico(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status respeitando as transições válidas")
    public OrdemServicoResponse alterarStatus(@PathVariable Long id,
            @Valid @RequestBody AlteracaoStatusRequest request) {
        return ordemServicoService.alterarStatus(id, request);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Consulta o histórico de status em ordem cronológica")
    public List<HistoricoStatusResponse> historico(@PathVariable Long id) {
        return ordemServicoService.historico(id);
    }
}
