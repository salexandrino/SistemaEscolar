package br.com.kutuar.academico.models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class Turma {
    private UUID id;
    private UUID tenantId;
    private UUID idAnoLetivo;
    private UUID idSerie;
    private String nome;
    private String turno;
    private String sala;
    private int capacidade;
    private String situacao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // ===== Censo Escolar (Educacenso) =====
    private String tipoMediador;
    private LocalTime horaInicio;
    private LocalTime horaTermino;
    private String diasSemana;
    private Integer cargaHorariaSemanal;
    private String tipoAtendimento;
    private String modalidadeEnsino;
    private String formaOrganizacao;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getIdAnoLetivo() { return idAnoLetivo; }
    public void setIdAnoLetivo(UUID idAnoLetivo) { this.idAnoLetivo = idAnoLetivo; }
    public UUID getIdSerie() { return idSerie; }
    public void setIdSerie(UUID idSerie) { this.idSerie = idSerie; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public int getCapacidade() { return capacidade; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getTipoMediador() { return tipoMediador; }
    public void setTipoMediador(String tipoMediador) { this.tipoMediador = tipoMediador; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraTermino() { return horaTermino; }
    public void setHoraTermino(LocalTime horaTermino) { this.horaTermino = horaTermino; }
    public String getDiasSemana() { return diasSemana; }
    public void setDiasSemana(String diasSemana) { this.diasSemana = diasSemana; }
    public Integer getCargaHorariaSemanal() { return cargaHorariaSemanal; }
    public void setCargaHorariaSemanal(Integer cargaHorariaSemanal) { this.cargaHorariaSemanal = cargaHorariaSemanal; }
    public String getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }
    public String getModalidadeEnsino() { return modalidadeEnsino; }
    public void setModalidadeEnsino(String modalidadeEnsino) { this.modalidadeEnsino = modalidadeEnsino; }
    public String getFormaOrganizacao() { return formaOrganizacao; }
    public void setFormaOrganizacao(String formaOrganizacao) { this.formaOrganizacao = formaOrganizacao; }
}