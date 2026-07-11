package br.com.synge.academico.dtos;

public class CriarProfessorDTO {
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Integer cargaHorariaContratual;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Integer getCargaHorariaContratual() { return cargaHorariaContratual; }
    public void setCargaHorariaContratual(Integer cargaHorariaContratual) { this.cargaHorariaContratual = cargaHorariaContratual; }
}
