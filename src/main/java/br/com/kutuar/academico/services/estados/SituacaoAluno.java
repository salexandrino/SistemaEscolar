package br.com.kutuar.academico.services.estados;

public enum SituacaoAluno implements EstadoAluno {
    ATIVO {
        @Override
        public boolean podeMudarPara(EstadoAluno novoEstado) {
            return novoEstado == TRANCADO || novoEstado == TRANSFERIDO || novoEstado == CONCLUIDO || novoEstado == CANCELADO;
        }
    },
    TRANCADO {
        @Override
        public boolean podeMudarPara(EstadoAluno novoEstado) {
            return novoEstado == ATIVO || novoEstado == TRANSFERIDO || novoEstado == CANCELADO;
        }
    },
    TRANSFERIDO {
        @Override
        public boolean podeMudarPara(EstadoAluno novoEstado) {
            return false; // estado final
        }
    },
    CONCLUIDO {
        @Override
        public boolean podeMudarPara(EstadoAluno novoEstado) {
            return false; // estado final
        }
    },
    CANCELADO {
        @Override
        public boolean podeMudarPara(EstadoAluno novoEstado) {
            return novoEstado == ATIVO; // pode ser reativado dependendo da regra, vamos permitir voltar pra ATIVO
        }
    };

    @Override
    public String getNome() {
        return this.name();
    }
}
