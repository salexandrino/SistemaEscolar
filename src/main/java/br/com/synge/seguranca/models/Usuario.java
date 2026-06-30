package br.com.synge.seguranca.models;

import br.com.synge.seguranca.enums.Perfil;

import java.time.LocalDateTime;
import java.util.UUID;

public class Usuario {
    private UUID id;
    private UUID tenantId; // OBRIGATÓRIO para Multi-Tenant

    private String nome;
    private Integer idade;
    private String cpf;
    private String senha; // Hash BCrypt

    private Perfil perfil;
    private Boolean ativo;

    private Integer tentativasLogin;
    private LocalDateTime bloqueadoAte;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtor vazio
    public Usuario() {
    }

    // Construtor completo (pode ser ajustado conforme a necessidade)
    public Usuario(UUID id, UUID tenantId, String nome, Integer idade, String cpf, String senha, Perfil perfil, Integer tentativasLogin, LocalDateTime bloqueadoAte, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.nome = nome;
        this.idade = idade;
        this.cpf = cpf;
        this.senha = senha;
        this.perfil = perfil;
        this.tentativasLogin = tentativasLogin;
        this.bloqueadoAte = bloqueadoAte;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Integer getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(Integer tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
    }

    public LocalDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }

    public void setBloqueadoAte(LocalDateTime bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // Método para mascarar CPF para logs
    public String getCpfMascarado() {
        if (cpf == null || cpf.length() < 11) {
            return cpf;
        }
        return "***." + cpf.substring(4, 7) + "." + cpf.substring(8, 11) + "-**";
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
