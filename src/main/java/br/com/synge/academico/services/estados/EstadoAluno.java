package br.com.synge.academico.services.estados;

public interface EstadoAluno {
    boolean podeMudarPara(EstadoAluno novoEstado);
    String getNome();
}
