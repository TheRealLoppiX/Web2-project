package br.edu.ifsertaope.assistencia.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsertaope.assistencia.dto.EquipamentoRequest;
import br.edu.ifsertaope.assistencia.dto.EquipamentoResponse;
import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.mapper.EquipamentoMapper;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.model.Equipamento;
import br.edu.ifsertaope.assistencia.repository.EquipamentoRepository;
import br.edu.ifsertaope.assistencia.repository.OrdemServicoRepository;

@Service
@Transactional(readOnly = true)
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteService clienteService;

    public EquipamentoService(EquipamentoRepository equipamentoRepository,
            OrdemServicoRepository ordemServicoRepository, ClienteService clienteService) {
        this.equipamentoRepository = equipamentoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteService = clienteService;
    }

    @Transactional
    public EquipamentoResponse criar(EquipamentoRequest request) {
        Cliente cliente = clienteService.obter(request.clienteId());
        Equipamento equipamento = new Equipamento();
        EquipamentoMapper.copiar(request, cliente, equipamento);
        return EquipamentoMapper.paraResponse(equipamentoRepository.save(equipamento));
    }

    public PaginaResponse<EquipamentoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        clienteService.obter(clienteId);
        return PaginaResponse.de(equipamentoRepository.findByClienteId(clienteId, pageable),
                EquipamentoMapper::paraResponse);
    }

    public EquipamentoResponse buscar(Long id) {
        return EquipamentoMapper.paraResponse(obter(id));
    }

    @Transactional
    public EquipamentoResponse atualizar(Long id, EquipamentoRequest request) {
        Equipamento equipamento = obter(id);
        Cliente cliente = clienteService.obter(request.clienteId());
        EquipamentoMapper.copiar(request, cliente, equipamento);
        return EquipamentoMapper.paraResponse(equipamento);
    }

    @Transactional
    public void remover(Long id) {
        Equipamento equipamento = obter(id);
        if (ordemServicoRepository.existsByEquipamentoId(id)) {
            throw new ConflitoException("Equipamento possui ordens de serviço e não pode ser removido.");
        }
        equipamentoRepository.delete(equipamento);
    }

    public Equipamento obter(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Equipamento com id " + id + " não encontrado."));
    }
}
