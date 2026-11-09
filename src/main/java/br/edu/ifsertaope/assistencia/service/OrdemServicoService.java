package br.edu.ifsertaope.assistencia.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsertaope.assistencia.dto.AlteracaoStatusRequest;
import br.edu.ifsertaope.assistencia.dto.AtribuicaoTecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.HistoricoStatusResponse;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoAberturaRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.OrdemServicoResponse;
import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.exception.RegraNegocioException;
import br.edu.ifsertaope.assistencia.mapper.OrdemServicoMapper;
import br.edu.ifsertaope.assistencia.mapper.Textos;
import br.edu.ifsertaope.assistencia.model.Equipamento;
import br.edu.ifsertaope.assistencia.model.HistoricoStatus;
import br.edu.ifsertaope.assistencia.model.OrdemServico;
import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;
import br.edu.ifsertaope.assistencia.model.Tecnico;
import br.edu.ifsertaope.assistencia.repository.HistoricoStatusRepository;
import br.edu.ifsertaope.assistencia.repository.OrdemServicoRepository;

@Service
@Transactional(readOnly = true)
public class OrdemServicoService {

    static final String OBSERVACAO_ABERTURA = "Abertura da ordem de serviço.";

    private final OrdemServicoRepository ordemServicoRepository;
    private final HistoricoStatusRepository historicoStatusRepository;
    private final EquipamentoService equipamentoService;
    private final TecnicoService tecnicoService;
    private final Clock clock;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
            HistoricoStatusRepository historicoStatusRepository, EquipamentoService equipamentoService,
            TecnicoService tecnicoService, Clock clock) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.historicoStatusRepository = historicoStatusRepository;
        this.equipamentoService = equipamentoService;
        this.tecnicoService = tecnicoService;
        this.clock = clock;
    }

    @Transactional
    public OrdemServicoResponse abrir(OrdemServicoAberturaRequest request) {
        Equipamento equipamento = equipamentoService.obter(request.equipamentoId());
        LocalDateTime agora = LocalDateTime.now(clock);

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setEquipamento(equipamento);
        ordemServico.setDescricaoDefeito(request.descricaoDefeito().trim());
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_ANALISE);
        ordemServico.setDataAbertura(agora);
        OrdemServico salva = ordemServicoRepository.save(ordemServico);

        registrarHistorico(salva, null, StatusOrdemServico.AGUARDANDO_ANALISE, agora, OBSERVACAO_ABERTURA);
        return OrdemServicoMapper.paraResponse(salva);
    }

    public PaginaResponse<OrdemServicoResponse> listar(StatusOrdemServico status, Long clienteId, Long tecnicoId,
            Pageable pageable) {
        return PaginaResponse.de(buscarComFiltros(status, clienteId, tecnicoId, pageable),
                OrdemServicoMapper::paraResponse);
    }

    private Page<OrdemServico> buscarComFiltros(StatusOrdemServico status, Long clienteId, Long tecnicoId,
            Pageable pageable) {
        boolean porStatus = status != null;
        boolean porCliente = clienteId != null;
        boolean porTecnico = tecnicoId != null;

        if (porStatus && porCliente && porTecnico) {
            return ordemServicoRepository.findByStatusAndEquipamentoClienteIdAndTecnicoId(status, clienteId,
                    tecnicoId, pageable);
        }
        if (porStatus && porCliente) {
            return ordemServicoRepository.findByStatusAndEquipamentoClienteId(status, clienteId, pageable);
        }
        if (porStatus && porTecnico) {
            return ordemServicoRepository.findByStatusAndTecnicoId(status, tecnicoId, pageable);
        }
        if (porCliente && porTecnico) {
            return ordemServicoRepository.findByEquipamentoClienteIdAndTecnicoId(clienteId, tecnicoId, pageable);
        }
        if (porStatus) {
            return ordemServicoRepository.findByStatus(status, pageable);
        }
        if (porCliente) {
            return ordemServicoRepository.findByEquipamentoClienteId(clienteId, pageable);
        }
        if (porTecnico) {
            return ordemServicoRepository.findByTecnicoId(tecnicoId, pageable);
        }
        return ordemServicoRepository.findAll(pageable);
    }

    public OrdemServicoResponse buscar(Long id) {
        return OrdemServicoMapper.paraResponse(obter(id));
    }

    @Transactional
    public OrdemServicoResponse atualizar(Long id, OrdemServicoAtualizacaoRequest request) {
        OrdemServico ordemServico = obter(id);
        validarAlteravel(ordemServico);
        validarValorTotal(request.valorTotal());

        String laudo = Textos.vazioParaNulo(request.laudoTecnico());
        String servicos = Textos.vazioParaNulo(request.descricaoServicos());
        if (laudo != null) {
            ordemServico.setLaudoTecnico(laudo);
        }
        if (servicos != null) {
            ordemServico.setDescricaoServicos(servicos);
        }
        if (request.valorTotal() != null) {
            ordemServico.setValorTotal(request.valorTotal());
        }
        return OrdemServicoMapper.paraResponse(ordemServico);
    }

    @Transactional
    public OrdemServicoResponse atribuirTecnico(Long id, AtribuicaoTecnicoRequest request) {
        OrdemServico ordemServico = obter(id);
        validarAlteravel(ordemServico);
        Tecnico tecnico = tecnicoService.obter(request.tecnicoId());
        if (!tecnico.isAtivo()) {
            throw new RegraNegocioException("Técnico inativo não pode receber OS.");
        }
        ordemServico.setTecnico(tecnico);
        return OrdemServicoMapper.paraResponse(ordemServico);
    }

    @Transactional
    public OrdemServicoResponse alterarStatus(Long id, AlteracaoStatusRequest request) {
        OrdemServico ordemServico = obter(id);
        validarAlteravel(ordemServico);

        StatusOrdemServico atual = ordemServico.getStatus();
        StatusOrdemServico destino = request.status();
        if (!atual.podeTransicionarPara(destino)) {
            throw new RegraNegocioException("Transição de " + atual + " para " + destino + " não permitida.");
        }
        validarRegrasDaEtapa(ordemServico, destino);

        LocalDateTime agora = LocalDateTime.now(clock);
        ordemServico.setStatus(destino);
        destino.aoEntrar(ordemServico, agora);
        registrarHistorico(ordemServico, atual, destino, agora, Textos.vazioParaNulo(request.observacao()));
        return OrdemServicoMapper.paraResponse(ordemServico);
    }

    public List<HistoricoStatusResponse> historico(Long id) {
        obter(id);
        return historicoStatusRepository.findByOrdemServicoIdOrderByDataHoraAscIdAsc(id).stream()
                .map(OrdemServicoMapper::paraResponse)
                .toList();
    }

    private OrdemServico obter(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ordem de serviço com id " + id + " não encontrada."));
    }

    private void validarAlteravel(OrdemServico ordemServico) {
        if (ordemServico.getStatus().isFinal()) {
            throw new RegraNegocioException("Ordem de serviço com status " + ordemServico.getStatus()
                    + " não aceita mais alterações.");
        }
    }

    private void validarValorTotal(BigDecimal valorTotal) {
        if (valorTotal != null && valorTotal.signum() < 0) {
            throw new RegraNegocioException("O valor total da OS não pode ser negativo.");
        }
    }

    private void validarRegrasDaEtapa(OrdemServico ordemServico, StatusOrdemServico destino) {
        switch (destino) {
            case ORCAMENTO_APROVADO -> {
                if (ordemServico.getLaudoTecnico() == null || ordemServico.getValorTotal() == null) {
                    throw new RegraNegocioException(
                            "Para aprovar o orçamento, a OS precisa ter laudo técnico e valor total preenchidos.");
                }
            }
            case EM_REPARO -> {
                Tecnico tecnico = ordemServico.getTecnico();
                if (tecnico == null || !tecnico.isAtivo()) {
                    throw new RegraNegocioException(
                            "Para iniciar o reparo, a OS precisa ter um técnico ativo atribuído.");
                }
            }
            case CONCLUIDO -> {
                if (ordemServico.getDescricaoServicos() == null) {
                    throw new RegraNegocioException(
                            "Para concluir a OS, a descrição dos serviços precisa estar preenchida.");
                }
            }
            default -> {
            }
        }
    }

    private void registrarHistorico(OrdemServico ordemServico, StatusOrdemServico anterior,
            StatusOrdemServico novo, LocalDateTime momento, String observacao) {
        historicoStatusRepository.save(new HistoricoStatus(ordemServico, anterior, novo, momento, observacao));
    }
}
