package br.com.kutuar.academico.services.estados;

public interface EstadoAluno {
    boolean podeMudarPara(EstadoAluno novoEstado);
    String getNome();
}
