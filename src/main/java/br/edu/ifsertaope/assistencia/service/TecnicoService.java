package br.edu.ifsertaope.assistencia.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsertaope.assistencia.dto.PaginaResponse;
import br.edu.ifsertaope.assistencia.dto.TecnicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoResponse;
import br.edu.ifsertaope.assistencia.exception.ConflitoException;
import br.edu.ifsertaope.assistencia.exception.RecursoNaoEncontradoException;
import br.edu.ifsertaope.assistencia.mapper.TecnicoMapper;
import br.edu.ifsertaope.assistencia.mapper.Textos;
import br.edu.ifsertaope.assistencia.model.Tecnico;
import br.edu.ifsertaope.assistencia.repository.TecnicoRepository;

@Service
@Transactional(readOnly = true)
public class TecnicoService {

    private static final String EMAIL_DUPLICADO = "Já existe um técnico com este e-mail.";

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public TecnicoResponse criar(TecnicoRequest request) {
        if (tecnicoRepository.existsByEmailIgnoreCase(Textos.normalizarEmail(request.email()))) {
            throw new ConflitoException(EMAIL_DUPLICADO);
        }
        return TecnicoMapper.paraResponse(tecnicoRepository.save(TecnicoMapper.paraEntidade(request)));
    }

    public PaginaResponse<TecnicoResponse> listar(Boolean ativos, Pageable pageable) {
        Page<Tecnico> pagina = ativos == null
                ? tecnicoRepository.findAll(pageable)
                : tecnicoRepository.findByAtivo(ativos, pageable);
        return PaginaResponse.de(pagina, TecnicoMapper::paraResponse);
    }

    public TecnicoResponse buscar(Long id) {
        return TecnicoMapper.paraResponse(obter(id));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoAtualizacaoRequest request) {
        Tecnico tecnico = obter(id);
        if (tecnicoRepository.existsByEmailIgnoreCaseAndIdNot(Textos.normalizarEmail(request.email()), id)) {
            throw new ConflitoException(EMAIL_DUPLICADO);
        }
        TecnicoMapper.copiar(request, tecnico);
        return TecnicoMapper.paraResponse(tecnico);
    }

    @Transactional
    public void desativar(Long id) {
        obter(id).setAtivo(false);
    }

    public Tecnico obter(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico com id " + id + " não encontrado."));
    }
}
