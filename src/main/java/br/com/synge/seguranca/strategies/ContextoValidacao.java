package br.com.synge.seguranca.strategies;

public class ContextoValidacao {
    private final ValidadorDocumento estrategia;

    public ContextoValidacao(ValidadorDocumento estrategia) {
        this.estrategia = estrategia;
    }

    public void executar(String documento) {
        estrategia.validar(documento);
    }
}