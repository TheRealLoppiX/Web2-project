package br.edu.ifsertaope.assistencia.model;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public enum StatusOrdemServico {

    AGUARDANDO_ANALISE {
        @Override
        public Set<StatusOrdemServico> proximosPermitidos() {
            return EnumSet.of(ORCAMENTO_APROVADO, CANCELADO);
        }
    },

    ORCAMENTO_APROVADO {
        @Override
        public Set<StatusOrdemServico> proximosPermitidos() {
            return EnumSet.of(EM_REPARO, CANCELADO);
        }
    },

    EM_REPARO {
        @Override
        public Set<StatusOrdemServico> proximosPermitidos() {
            return EnumSet.of(CONCLUIDO);
        }
    },

    CONCLUIDO {
        @Override
        public Set<StatusOrdemServico> proximosPermitidos() {
            return EnumSet.of(ENTREGUE);
        }

        @Override
        public void aoEntrar(OrdemServico ordemServico, LocalDateTime momento) {
            ordemServico.setDataConclusao(momento);
        }
    },

    ENTREGUE {
        @Override
        public Set<StatusOrdemServico> proximosPermitidos() {
            return EnumSet.noneOf(StatusOrdemServico.class);
        }

        @Override
        public void aoEntrar(OrdemServico ordemServico, LocalDateTime momento) {
            ordemServico.setDataEntrega(momento);
        }
    },

    CANCELADO {
        @Override
        public Set<StatusOrdemServico> proximosPermitidos() {
            return EnumSet.noneOf(StatusOrdemServico.class);
        }
    };

    public abstract Set<StatusOrdemServico> proximosPermitidos();

    public boolean podeTransicionarPara(StatusOrdemServico destino) {
        return destino != null && proximosPermitidos().contains(destino);
    }

    public boolean isFinal() {
        return proximosPermitidos().isEmpty();
    }

    public void aoEntrar(OrdemServico ordemServico, LocalDateTime momento) {
    }
}
