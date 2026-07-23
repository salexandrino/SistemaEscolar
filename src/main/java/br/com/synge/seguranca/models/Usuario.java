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

    private boolean aprovado;

    private LocalDateTime bloqueadoAte;
    private int tentativasLogin;

    private LocalDateTime ultimoLogin;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

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
        // Normaliza para só dígitos. Sem isso, telas diferentes (login sempre manda
        // com máscara "000.000.000-00"; cadastro de escola manda cru, sem máscara)
        // gravavam/consultavam formatos diferentes do "mesmo" CPF, e o WHERE cpf = ?
        // no login não batia — aparecia "CPF não cadastrado" mesmo sendo o CPF certo.
        this.cpf = cpf != null ? cpf.replaceAll("\\D", "") : null;
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
    public String getStatus() {
        return aprovado ? "APROVADO" : "PENDENTE";
    }

    public String getPerfilDescricao() {
        return perfil != null ? perfil.name() : "";
    }

    public boolean isPendente() {
        return !aprovado;
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

    /**
     * CPF completo, formatado (000.000.000-00), para exibição nas telas.
     * O cpf em si agora é sempre gravado só com dígitos (ver setCpf), então
     * as telas não podem mais exibir "usuario.cpf" cru sem formatação.
     */
    public String getCpfFormatado() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    public String getEmailMascarado() {

        if (email == null || email.isBlank()) {
            return "";
        }

        int arroba = email.indexOf("@");

        if (arroba <= 1) {
            return "***";
        }

        return email.substring(0,1)
                + "***"
                + email.substring(arroba);
    }

}