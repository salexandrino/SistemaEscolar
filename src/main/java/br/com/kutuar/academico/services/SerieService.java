package br.com.kutuar.academico.services;

import br.com.kutuar.academico.dtos.AtualizarSerieDTO;
import br.com.kutuar.academico.dtos.CriarSerieDTO;
import br.com.kutuar.academico.dtos.SerieResponseDTO;
import br.com.kutuar.academico.models.Serie;
import br.com.kutuar.academico.repositories.AnoLetivoRepository;
import br.com.kutuar.academico.repositories.SerieRepository;
import br.com.kutuar.academico.repositories.TurmaRepository;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SerieService {

    private static final Logger logger = LoggerFactory.getLogger(SerieService.class);

    private static final List<String> ETAPAS_VALIDAS = List.of(
            "INFANTIL_CRECHE", "INFANTIL_PRE_ESCOLA", "FUNDAMENTAL_ANOS_INICIAIS",
            "FUNDAMENTAL_ANOS_FINAIS", "MEDIO", "EJA", "TECNICO"
    );

    private final SerieRepository repository;
    private final AnoLetivoRepository anoLetivoRepository;
    private final TurmaRepository turmaRepository;

    public SerieService(SerieRepository repository, AnoLetivoRepository anoLetivoRepository, TurmaRepository turmaRepository) {
        this.repository = repository;
        this.anoLetivoRepository = anoLetivoRepository;
        this.turmaRepository = turmaRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    private String validarEtapa(String etapaEnsino) {
        if (etapaEnsino == null || etapaEnsino.isBlank()) return null; // opcional
        String v = etapaEnsino.toUpperCase();
        if (!ETAPAS_VALIDAS.contains(v)) {
            throw new ValidationException("etapaEnsino inválida. Use: " + ETAPAS_VALIDAS);
        }
        return v;
    }

    public SerieResponseDTO criar(CriarSerieDTO dto) {
        if (dto == null || dto.getIdAnoLetivo() == null) throw new ValidationException("Ano letivo é obrigatório.");
        if (dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        String etapaEnsino = validarEtapa(dto.getEtapaEnsino());
        UUID tenantId = tenant();

        var anoLetivo = anoLetivoRepository.buscarPorId(tenantId, dto.getIdAnoLetivo())
                .orElseThrow(() -> new NotFoundException("Ano letivo não encontrado."));

        if (!anoLetivo.isAtivo()) {
            throw new ValidationException("Não é possível criar série em um Ano Letivo que não está ATIVO. Status atual: " + anoLetivo.getSituacao());
        }

        if (repository.existsPorNomeAno(tenantId, dto.getIdAnoLetivo(), dto.getNome()))
            throw new ConflictException("Já existe uma série com este nome neste ano letivo.");

        logger.info("Criando série '{}' no ano letivo {} para tenant {}", dto.getNome(), dto.getIdAnoLetivo(), tenantId);
        Serie s = repository.criar(tenantId, dto.getIdAnoLetivo(), dto.getNome().trim(), etapaEnsino);
        return toDto(s);
    }

    public SerieResponseDTO obter(UUID id) {
        return toDto(repository.buscarPorId(tenant(), id).orElseThrow(() -> new NotFoundException("Série não encontrada.")));
    }

    public List<SerieResponseDTO> listarPorAno(UUID idAnoLetivo) {
        UUID tenantId = tenant();
        anoLetivoRepository.buscarPorId(tenantId, idAnoLetivo).orElseThrow(() -> new NotFoundException("Ano letivo não encontrado."));
        return repository.listarPorAnoLetivo(tenantId, idAnoLetivo).stream().map(this::toDto).collect(Collectors.toList());
    }

    public void atualizar(UUID id, AtualizarSerieDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        String etapaEnsino = validarEtapa(dto.getEtapaEnsino());
        UUID tenantId = tenant();
        Serie atual = repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Série não encontrada."));
        if (!atual.getNome().equalsIgnoreCase(dto.getNome()) && repository.existsPorNomeAno(tenantId, atual.getIdAnoLetivo(), dto.getNome()))
            throw new ConflictException("Já existe uma série com este nome neste ano letivo.");
        logger.info("Atualizando série '{}' (id: {}) para tenant {}", atual.getNome(), id, tenantId);
        repository.atualizar(tenantId, id, dto.getNome().trim(), etapaEnsino);
    }

    public void remover(UUID id) {
        UUID tenantId = tenant();
        Serie serie = repository.buscarPorId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Série não encontrada."));

        int turmasRelacionadas = turmaRepository.contarPorSerie(tenantId, id);
        if (turmasRelacionadas > 0) {
            throw new ConflictException("Não é possível remover uma série que possui " + turmasRelacionadas + " turma(s) relacionada(s). Remova as turmas antes de deletar a série.");
        }

        logger.info("Removendo série '{}' (id: {}) do tenant {}", serie.getNome(), id, tenantId);
        repository.remover(tenantId, id);
    }

    private SerieResponseDTO toDto(Serie s) {
        SerieResponseDTO d = new SerieResponseDTO();
        d.setId(s.getId());
        d.setIdAnoLetivo(s.getIdAnoLetivo());
        d.setNome(s.getNome());
        d.setEtapaEnsino(s.getEtapaEnsino());
        d.setCriadoEm(s.getCriadoEm());
        d.setAtualizadoEm(s.getAtualizadoEm());
        return d;
    }
}