package br.edu.ifsertaope.assistencia.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsertaope.assistencia.dto.ClienteRequest;
import br.edu.ifsertaope.assistencia.dto.ClienteResponse;
import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.mapper.ClienteMapper;
import br.edu.ifsertaope.assistencia.mapper.Textos;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.repository.ClienteRepository;
import br.edu.ifsertaope.assistencia.repository.EquipamentoRepository;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final Clock clock;

    public ClienteService(ClienteRepository clienteRepository, EquipamentoRepository equipamentoRepository,
            Clock clock) {
        this.clienteRepository = clienteRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.clock = clock;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        validarUnicidade(request, null);
        Cliente cliente = new Cliente();
        ClienteMapper.copiar(request, cliente);
        cliente.setDataCadastro(LocalDateTime.now(clock));
        return ClienteMapper.paraResponse(clienteRepository.save(cliente));
    }

    public PaginaResponse<ClienteResponse> listar(String nome, Pageable pageable) {
        Page<Cliente> pagina = nome == null || nome.isBlank()
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        return PaginaResponse.de(pagina, ClienteMapper::paraResponse);
    }

    public ClienteResponse buscar(Long id) {
        return ClienteMapper.paraResponse(obter(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = obter(id);
        validarUnicidade(request, id);
        ClienteMapper.copiar(request, cliente);
        return ClienteMapper.paraResponse(cliente);
    }

    @Transactional
    public void remover(Long id) {
        Cliente cliente = obter(id);
        if (equipamentoRepository.existsByClienteId(id)) {
            throw new ConflitoException("Cliente possui equipamentos cadastrados e não pode ser removido.");
        }
        clienteRepository.delete(cliente);
    }

    public Cliente obter(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com id " + id + " não encontrado."));
    }

    private void validarUnicidade(ClienteRequest request, Long idAtual) {
        String email = Textos.normalizarEmail(request.email());
        boolean emailEmUso = idAtual == null
                ? clienteRepository.existsByEmailIgnoreCase(email)
                : clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, idAtual);
        if (emailEmUso) {
            throw new ConflitoException("Já existe um cliente com este e-mail.");
        }
        String cpfCnpj = Textos.vazioParaNulo(request.cpfCnpj());
        if (cpfCnpj == null) {
            return;
        }
        boolean documentoEmUso = idAtual == null
                ? clienteRepository.existsByCpfCnpj(cpfCnpj)
                : clienteRepository.existsByCpfCnpjAndIdNot(cpfCnpj, idAtual);
        if (documentoEmUso) {
            throw new ConflitoException("Já existe um cliente com este CPF/CNPJ.");
        }
    }
}
