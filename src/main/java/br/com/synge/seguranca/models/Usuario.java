package br.com.synge.seguranca.models;

import br.com.synge.seguranca.enums.Perfil;

import java.time.LocalDateTime;
import java.util.UUID;

public class Usuario {

    private UUID id;
    private UUID tenantId;
    private UUID escolaId;

    private String nomeCompleto;
    private String email;
    private String cpf;
    private String telefone;
    private String senhaHash;
    private String confirmacaoSenha;

    private Perfil perfil;

    // Usuário aprovado para acessar o sistema
    private boolean aprovado;

    // Controle de bloqueio
    private LocalDateTime bloqueadoAte;
    private int tentativasLogin;

    // Datas
    private LocalDateTime ultimoLogin;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Recuperação de senha
    private String resetPasswordToken;
    private LocalDateTime resetPasswordExpiresAt;

    public Usuario() {
    }

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

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }

    /* ==========================================================
       APROVAÇÃO
       ========================================================== */

    public boolean isAprovado() {
        return aprovado;
    }

    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }

    /*
     * Métodos mantidos por compatibilidade com o Repository
     */

    public boolean isAtivo() {
        return aprovado;
    }

    public void setAtivo(boolean ativo) {
        this.aprovado = ativo;
    }

    /* ==========================================================
       BLOQUEIO
       ========================================================== */

    public LocalDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }

    public void setBloqueadoAte(LocalDateTime bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }

    public boolean isBloqueado() {
        return bloqueadoAte != null &&
                bloqueadoAte.isAfter(LocalDateTime.now());
    }

    /**
     * Compatibilidade com o Repository.
     * Se false -> desbloqueia.
     * Se true -> bloqueia por tempo indeterminado.
     */
    public void setBloqueado(boolean bloqueado) {
        if (bloqueado) {
            this.bloqueadoAte = LocalDateTime.MAX;
        } else {
            this.bloqueadoAte = null;
        }
    }

    public int getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(int tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
    }

    /* ==========================================================
       DATAS
       ========================================================== */

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
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

    /* ==========================================================
       RECUPERAÇÃO DE SENHA
       ========================================================== */

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordExpiresAt() {
        return resetPasswordExpiresAt;
    }

    public void setResetPasswordExpiresAt(LocalDateTime resetPasswordExpiresAt) {
        this.resetPasswordExpiresAt = resetPasswordExpiresAt;
    }

    /* ==========================================================
       LOGS
       ========================================================== */

    public String getCpfMascarado() {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }

        return "***." + cpf.substring(3, 6) + ".***-**";
    }

    public String getEmailMascarado() {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] partes = email.split("@");

        if (partes[0].length() <= 2) {
            return "***@" + partes[1];
        }

        return partes[0].substring(0, 2) + "***@" + partes[1];
    }
}