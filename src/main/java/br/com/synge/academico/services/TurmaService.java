package br.com.synge.academico.services;

import br.com.synge.academico.dtos.CriarTurmaDTO;
import br.com.synge.academico.dtos.EditarTurmaDTO;
import br.com.synge.academico.dtos.TurmaResponseDTO;
import br.com.synge.academico.models.Turma;
import br.com.synge.academico.repositories.AnoLetivoRepository;
import br.com.synge.academico.repositories.SerieRepository;
import br.com.synge.academico.repositories.TurmaRepository;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TurmaService {

    private static final List<String> MEDIADORES_VALIDOS = List.of("PRESENCIAL", "EAD", "SEMIPRESENCIAL");
    private static final List<String> ATENDIMENTOS_VALIDOS = List.of("ESCOLARIZACAO", "AEE", "ATIVIDADE_COMPLEMENTAR");
    private static final List<String> MODALIDADES_VALIDAS = List.of("REGULAR", "EDUCACAO_ESPECIAL", "EJA", "PROFISSIONAL");
    private static final List<String> ORGANIZACOES_VALIDAS = List.of("SERIE_ANO", "CICLO", "PERIODOS_SEMESTRAIS", "MODULOS", "MULTISSERIADO");

    private final TurmaRepository turmaRepository;
    private final AnoLetivoRepository anoLetivoRepository;
    private final SerieRepository serieRepository;

    public TurmaService(TurmaRepository turmaRepository, AnoLetivoRepository anoLetivoRepository, SerieRepository serieRepository) {
        this.turmaRepository = turmaRepository;
        this.anoLetivoRepository = anoLetivoRepository;
        this.serieRepository = serieRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public TurmaResponseDTO criar(CriarTurmaDTO dto) {
        if (dto == null) throw new ValidationException("Dados obrigatórios ausentes.");
        if (dto.getIdAnoLetivo() == null) throw new ValidationException("idAnoLetivo é obrigatório.");
        if (dto.getIdSerie() == null) throw new ValidationException("idSerie é obrigatório.");
        if (dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("nome da turma é obrigatório.");
        if (dto.getTurno() == null || dto.getTurno().isBlank()) throw new ValidationException("turno é obrigatório.");
        if (dto.getCapacidade() == null || dto.getCapacidade() < 0) throw new ValidationException("capacidade deve ser >= 0.");

        String tipoMediador = valorOuPadrao(dto.getTipoMediador(), "PRESENCIAL");
        String tipoAtendimento = valorOuPadrao(dto.getTipoAtendimento(), "ESCOLARIZACAO");
        String modalidadeEnsino = valorOuPadrao(dto.getModalidadeEnsino(), "REGULAR");
        String formaOrganizacao = valorOuPadrao(dto.getFormaOrganizacao(), "SERIE_ANO");

        if (!MEDIADORES_VALIDOS.contains(tipoMediador)) throw new ValidationException("tipoMediador inválido. Use: " + MEDIADORES_VALIDOS);
        if (!ATENDIMENTOS_VALIDOS.contains(tipoAtendimento)) throw new ValidationException("tipoAtendimento inválido. Use: " + ATENDIMENTOS_VALIDOS);
        if (!MODALIDADES_VALIDAS.contains(modalidadeEnsino)) throw new ValidationException("modalidadeEnsino inválida. Use: " + MODALIDADES_VALIDAS);
        if (!ORGANIZACOES_VALIDAS.contains(formaOrganizacao)) throw new ValidationException("formaOrganizacao inválida. Use: " + ORGANIZACOES_VALIDAS);
        if (dto.getHoraInicio() != null && dto.getHoraTermino() != null && !dto.getHoraInicio().isBefore(dto.getHoraTermino())) {
            throw new ValidationException("horaInicio deve ser anterior a horaTermino.");
        }

        UUID tenantId = tenant();

        var anoLetivo = anoLetivoRepository.buscarPorId(tenantId, dto.getIdAnoLetivo())
                .orElseThrow(() -> new ValidationException("Ano letivo inválido para este tenant."));

        if (!anoLetivo.isAtivo()) {
            throw new ValidationException("Não é possível criar turma em um Ano Letivo que não está ATIVO. Status atual: " + anoLetivo.getSituacao());
        }

        var serie = serieRepository.buscarPorId(tenantId, dto.getIdSerie())
                .orElseThrow(() -> new ValidationException("Série inválida para este tenant."));

        if (!serie.getIdAnoLetivo().equals(anoLetivo.getId())) {
            throw new ValidationException("Série não pertence ao Ano Letivo informado.");
        }

        Turma t = new Turma();
        t.setId(UUID.randomUUID());
        t.setTenantId(tenantId);
        t.setIdAnoLetivo(dto.getIdAnoLetivo());
        t.setIdSerie(dto.getIdSerie());
        t.setNome(dto.getNome());
        t.setTurno(dto.getTurno());
        t.setSala(dto.getSala());
        t.setCapacidade(dto.getCapacidade());
        t.setSituacao("ATIVA");
        t.setCriadoEm(LocalDateTime.now());
        t.setAtualizadoEm(LocalDateTime.now());
        t.setTipoMediador(tipoMediador);
        t.setHoraInicio(dto.getHoraInicio());
        t.setHoraTermino(dto.getHoraTermino());
        t.setDiasSemana(dto.getDiasSemana());
        t.setCargaHorariaSemanal(dto.getCargaHorariaSemanal());
        t.setTipoAtendimento(tipoAtendimento);
        t.setModalidadeEnsino(modalidadeEnsino);
        t.setFormaOrganizacao(formaOrganizacao);

        Turma criada = turmaRepository.criar(t);
        return toDto(criada);
    }

    private String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.isBlank()) ? padrao : valor.toUpperCase();
    }

    public void encerrar(UUID idTurma) {
        UUID tenantId = tenant();
        turmaRepository.buscarPorId(tenantId, idTurma).orElseThrow(() -> new NotFoundException("Turma não encontrada."));
        turmaRepository.encerrar(tenantId, idTurma);
    }

    public List<TurmaResponseDTO> listar(UUID idAnoLetivo, UUID idSerie) {
        UUID tenantId = tenant();
        return turmaRepository.listar(tenantId, idAnoLetivo, idSerie).stream().map(this::toDto).collect(Collectors.toList());
    }

    public int consultarCapacidadeMatriculados(UUID idTurma) {
        UUID tenantId = tenant();
        turmaRepository.buscarPorId(tenantId, idTurma).orElseThrow(() -> new NotFoundException("Turma não encontrada."));
        return turmaRepository.contarMatriculas(tenantId, idTurma);
    }

    public Turma validarDisponibilidadeParaMatricula(UUID tenantId, UUID idTurma) {
        Turma turma = turmaRepository.buscarPorId(tenantId, idTurma)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada."));

        if (!"ATIVA".equals(turma.getSituacao())) {
            throw new ValidationException("Não é possível matricular: a turma está encerrada.");
        }

        var anoLetivo = anoLetivoRepository.buscarPorId(tenantId, turma.getIdAnoLetivo())
                .orElseThrow(() -> new ValidationException("Ano letivo da turma não encontrado."));
        if (!anoLetivo.isAtivo() || "ARQUIVADO".equals(anoLetivo.getSituacao())) {
            throw new ValidationException("Não é possível matricular: o ano letivo desta turma está arquivado.");
        }

        int matriculados = turmaRepository.contarMatriculas(tenantId, idTurma);
        if (matriculados >= turma.getCapacidade()) {
            throw new ValidationException("Não é possível matricular: a turma atingiu sua capacidade máxima (" + turma.getCapacidade() + " vagas).");
        }

        return turma;
    }

    public TurmaResponseDTO buscarPorId(UUID id) {
        UUID tenantId = tenant();
        return turmaRepository.buscarPorId(tenantId, id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada."));
    }

    public void editar(UUID id, EditarTurmaDTO dto) {
        UUID tenantId = tenant();
        Turma turma = turmaRepository.buscarPorId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada."));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            turma.setNome(dto.getNome());
        }
        if (dto.getTurno() != null && !dto.getTurno().isBlank()) {
            turma.setTurno(dto.getTurno());
        }
        if (dto.getSala() != null) {
            turma.setSala(dto.getSala());
        }
        if (dto.getCapacidade() != null && dto.getCapacidade() >= 0) {
            turma.setCapacidade(dto.getCapacidade());
        }
        if (dto.getTipoMediador() != null && MEDIADORES_VALIDOS.contains(dto.getTipoMediador().toUpperCase())) {
            turma.setTipoMediador(dto.getTipoMediador().toUpperCase());
        }
        if (dto.getHoraInicio() != null) {
            turma.setHoraInicio(dto.getHoraInicio());
        }
        if (dto.getHoraTermino() != null) {
            turma.setHoraTermino(dto.getHoraTermino());
        }
        if (dto.getDiasSemana() != null) {
            turma.setDiasSemana(dto.getDiasSemana());
        }
        if (dto.getCargaHorariaSemanal() != null) {
            turma.setCargaHorariaSemanal(dto.getCargaHorariaSemanal());
        }
        if (dto.getTipoAtendimento() != null && ATENDIMENTOS_VALIDOS.contains(dto.getTipoAtendimento().toUpperCase())) {
            turma.setTipoAtendimento(dto.getTipoAtendimento().toUpperCase());
        }
        if (dto.getModalidadeEnsino() != null && MODALIDADES_VALIDAS.contains(dto.getModalidadeEnsino().toUpperCase())) {
            turma.setModalidadeEnsino(dto.getModalidadeEnsino().toUpperCase());
        }
        if (dto.getFormaOrganizacao() != null && ORGANIZACOES_VALIDAS.contains(dto.getFormaOrganizacao().toUpperCase())) {
            turma.setFormaOrganizacao(dto.getFormaOrganizacao().toUpperCase());
        }

        turma.setAtualizadoEm(LocalDateTime.now());
        turmaRepository.atualizar(turma);
    }

    public void apagar(UUID id) {
        UUID tenantId = tenant();
        Turma turma = turmaRepository.buscarPorId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada."));

        int matriculasAtivas = turmaRepository.contarMatriculas(tenantId, id);
        if (matriculasAtivas > 0) {
            throw new ConflictException("Não é possível apagar: existem " + matriculasAtivas + " matrícula(s) ativa(s) nesta turma.");
        }

        turmaRepository.apagar(tenantId, id);
    }

    private TurmaResponseDTO toDto(Turma t) {
        TurmaResponseDTO d = new TurmaResponseDTO();
        d.setId(t.getId());
        d.setIdAnoLetivo(t.getIdAnoLetivo());
        d.setIdSerie(t.getIdSerie());
        d.setNome(t.getNome());
        d.setTurno(t.getTurno());
        d.setSala(t.getSala());
        d.setCapacidade(t.getCapacidade());
        d.setSituacao(t.getSituacao());
        d.setCriadoEm(t.getCriadoEm());
        d.setAtualizadoEm(t.getAtualizadoEm());
        d.setTipoMediador(t.getTipoMediador());
        d.setHoraInicio(t.getHoraInicio());
        d.setHoraTermino(t.getHoraTermino());
        d.setDiasSemana(t.getDiasSemana());
        d.setCargaHorariaSemanal(t.getCargaHorariaSemanal());
        d.setTipoAtendimento(t.getTipoAtendimento());
        d.setModalidadeEnsino(t.getModalidadeEnsino());
        d.setFormaOrganizacao(t.getFormaOrganizacao());
        return d;
    }
}