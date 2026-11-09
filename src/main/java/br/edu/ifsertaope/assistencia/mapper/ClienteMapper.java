package br.edu.ifsertaope.assistencia.mapper;

import br.edu.ifsertaope.assistencia.dto.ClienteRequest;
import br.edu.ifsertaope.assistencia.dto.ClienteResponse;
import br.edu.ifsertaope.assistencia.model.Cliente;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static void copiar(ClienteRequest origem, Cliente destino) {
        destino.setNome(origem.nome().trim());
        destino.setTelefone(origem.telefone().trim());
        destino.setEmail(Textos.normalizarEmail(origem.email()));
        destino.setCpfCnpj(Textos.vazioParaNulo(origem.cpfCnpj()));
    }

    public static ClienteResponse paraResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getCpfCnpj(),
                cliente.getDataCadastro());
    }
}
