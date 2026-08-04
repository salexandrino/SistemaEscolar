package br.com.synge.academico.dtos;

import java.time.LocalTime;
import java.util.UUID;

public class CriarTurmaDTO {
    private UUID idAnoLetivo;
    private UUID idSerie;
    private String nome;
    private String turno;
    private String sala;
    private Integer capacidade;

    private String tipoMediador;
    private LocalTime horaInicio;
    private LocalTime horaTermino;
    private String diasSemana;
    private Integer cargaHorariaSemanal;
    private String tipoAtendimento;
    private String modalidadeEnsino;
    private String formaOrganizacao;

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
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
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