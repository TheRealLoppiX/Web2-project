package br.edu.ifsertaope.assistencia.mapper;

import br.edu.ifsertaope.assistencia.dto.EquipamentoRequest;
import br.edu.ifsertaope.assistencia.dto.EquipamentoResponse;
import br.edu.ifsertaope.assistencia.model.Cliente;
import br.edu.ifsertaope.assistencia.model.Equipamento;

public final class EquipamentoMapper {

    private EquipamentoMapper() {
    }

    public static void copiar(EquipamentoRequest origem, Cliente cliente, Equipamento destino) {
        destino.setCliente(cliente);
        destino.setTipo(origem.tipo());
        destino.setMarca(origem.marca().trim());
        destino.setModelo(origem.modelo().trim());
        destino.setNumeroSerie(Textos.vazioParaNulo(origem.numeroSerie()));
        destino.setEspecificacoes(Textos.vazioParaNulo(origem.especificacoes()));
    }

    public static EquipamentoResponse paraResponse(Equipamento equipamento) {
        Cliente cliente = equipamento.getCliente();
        return new EquipamentoResponse(
                equipamento.getId(),
                cliente.getId(),
                cliente.getNome(),
                equipamento.getTipo(),
                equipamento.getMarca(),
                equipamento.getModelo(),
                equipamento.getNumeroSerie(),
                equipamento.getEspecificacoes());
    }

    public static String descrever(Equipamento equipamento) {
        return equipamento.getTipo() + " " + equipamento.getMarca() + " " + equipamento.getModelo();
    }
}
