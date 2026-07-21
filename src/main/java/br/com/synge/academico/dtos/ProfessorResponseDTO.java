package br.com.synge.academico.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProfessorResponseDTO {
    private UUID id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private int cargaHorariaContratual;
    private boolean ativo;
    private LocalDateTime criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public int getCargaHorariaContratual() { return cargaHorariaContratual; }
    public void setCargaHorariaContratual(int cargaHorariaContratual) { this.cargaHorariaContratual = cargaHorariaContratual; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
