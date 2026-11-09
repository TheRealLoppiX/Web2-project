package br.edu.ifsertaope.assistencia.repository;

import org.springframework.data.jpa.domain.Specification;

import br.edu.ifsertaope.assistencia.model.OrdemServico;
import br.edu.ifsertaope.assistencia.model.StatusOrdemServico;

public final class OrdemServicoSpecifications {

    private OrdemServicoSpecifications() {
    }

    public static Specification<OrdemServico> filtrar(StatusOrdemServico status, Long clienteId, Long tecnicoId) {
        return Specification.allOf(comStatus(status), doCliente(clienteId), doTecnico(tecnicoId));
    }

    private static Specification<OrdemServico> comStatus(StatusOrdemServico status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<OrdemServico> doCliente(Long clienteId) {
        return (root, query, cb) -> clienteId == null
                ? null
                : cb.equal(root.get("equipamento").get("cliente").get("id"), clienteId);
    }

    private static Specification<OrdemServico> doTecnico(Long tecnicoId) {
        return (root, query, cb) -> tecnicoId == null ? null : cb.equal(root.get("tecnico").get("id"), tecnicoId);
    }
}
