package br.edu.ifsertaope.assistencia.mapper;

import br.edu.ifsertaope.assistencia.dto.TecnicoAtualizacaoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoRequest;
import br.edu.ifsertaope.assistencia.dto.TecnicoResponse;
import br.edu.ifsertaope.assistencia.model.Tecnico;

public final class TecnicoMapper {

    private TecnicoMapper() {
    }

    public static Tecnico paraEntidade(TecnicoRequest origem) {
        Tecnico tecnico = new Tecnico();
        tecnico.setNome(origem.nome().trim());
        tecnico.setEmail(Textos.normalizarEmail(origem.email()));
        tecnico.setEspecialidade(origem.especialidade().trim());
        tecnico.setAtivo(true);
        return tecnico;
    }

    public static void copiar(TecnicoAtualizacaoRequest origem, Tecnico destino) {
        destino.setNome(origem.nome().trim());
        destino.setEmail(Textos.normalizarEmail(origem.email()));
        destino.setEspecialidade(origem.especialidade().trim());
        if (origem.ativo() != null) {
            destino.setAtivo(origem.ativo());
        }
    }

    public static TecnicoResponse paraResponse(Tecnico tecnico) {
        return new TecnicoResponse(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getEmail(),
                tecnico.getEspecialidade(),
                tecnico.isAtivo());
    }
}
