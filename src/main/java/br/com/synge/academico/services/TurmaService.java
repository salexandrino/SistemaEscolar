package br.com.synge.academico.services;

import br.com.synge.academico.dtos.CriarTurmaDTO;
import br.com.synge.academico.dtos.TurmaResponseDTO;
import br.com.synge.academico.models.Turma;
import br.com.synge.academico.repositories.AnoLetivoRepository;
import br.com.synge.academico.repositories.SerieRepository;
import br.com.synge.academico.repositories.TurmaRepository;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TurmaService {

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

        UUID tenantId = tenant();

        // valida Ano Letivo e Série
        var anoLetivo = anoLetivoRepository.buscarPorId(tenantId, dto.getIdAnoLetivo())
                .orElseThrow(() -> new ValidationException("Ano letivo inválido para este tenant."));
        var serie = serieRepository.buscarPorId(tenantId, dto.getIdSerie())
                .orElseThrow(() -> new ValidationException("Série inválida para este tenant."));

        // Série deve pertencer ao mesmo Ano Letivo informado
        if (!serie.getIdAnoLetivo().equals(anoLetivo.getId())) {
            throw new ValidationException("Série não pertence ao Ano Letivo informado.");
        }

        Turma criada = turmaRepository.criar(tenantId, dto.getIdAnoLetivo(), dto.getIdSerie(), dto.getNome(), dto.getTurno(), dto.getSala(), dto.getCapacidade());
        return toDto(criada);
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

    /**
     * Valida se uma turma está apta a receber uma nova matrícula:
     * - a turma precisa existir e estar com situação ATIVA (não ENCERRADA);
     * - o ano letivo ao qual ela pertence precisa estar ATIVO (não ARQUIVADO);
     * - precisa haver vaga disponível (matriculados < capacidade).
     * Lança ValidationException com a razão específica caso alguma regra falhe.
     */
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
        return d;
    }
}