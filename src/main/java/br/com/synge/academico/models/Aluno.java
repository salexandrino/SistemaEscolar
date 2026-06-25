package br.com.synge.academico.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alunos")
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID escolaId;
    private UUID turmaId;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String corPele;
    private String deficiencias;
    private String endereco;
    private String status;
    private LocalDateTime dataCadastro;

    // Construtor padrão
    public Aluno() {
    }

    // Construtor completo
    public Aluno(UUID id, UUID escolaId, UUID turmaId, String nome, String cpf, LocalDate dataNascimento, String corPele, String deficiencias, String endereco, String status, LocalDateTime dataCadastro) {
        this.id = id;
        this.escolaId = escolaId;
        this.turmaId = turmaId;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.corPele = corPele;
        this.deficiencias = deficiencias;
        this.endereco = endereco;
        this.status = status;
        this.dataCadastro = dataCadastro;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(UUID turmaId) {
        this.turmaId = turmaId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCorPele() {
        return corPele;
    }

    public void setCorPele(String corPele) {
        this.corPele = corPele;
    }

    public String getDeficiencias() {
        return deficiencias;
    }

    public void setDeficiencias(String deficiencias) {
        this.deficiencias = deficiencias;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
