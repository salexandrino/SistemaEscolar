package br.com.synge.academico.services;

import br.com.synge.academico.dtos.AtualizarSerieDTO;
import br.com.synge.academico.dtos.CriarSerieDTO;
import br.com.synge.academico.dtos.SerieResponseDTO;
import br.com.synge.academico.models.Serie;
import br.com.synge.academico.repositories.AnoLetivoRepository;
import br.com.synge.academico.repositories.SerieRepository;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SerieService {

    private final SerieRepository repository;
    private final AnoLetivoRepository anoLetivoRepository;

    public SerieService(SerieRepository repository, AnoLetivoRepository anoLetivoRepository) {
        this.repository = repository;
        this.anoLetivoRepository = anoLetivoRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public SerieResponseDTO criar(CriarSerieDTO dto) {
        if (dto == null || dto.getIdAnoLetivo() == null) throw new ValidationException("Ano letivo é obrigatório.");
        if (dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        UUID tenantId = tenant();

        // validar que ano letivo existe no tenant
        anoLetivoRepository.buscarPorId(tenantId, dto.getIdAnoLetivo()).orElseThrow(() -> new NotFoundException("Ano letivo não encontrado."));

        if (repository.existsPorNomeAno(tenantId, dto.getIdAnoLetivo(), dto.getNome()))
            throw new ConflictException("Já existe uma série com este nome neste ano letivo.");

        Serie s = repository.criar(tenantId, dto.getIdAnoLetivo(), dto.getNome().trim());
        return toDto(s);
    }

    public SerieResponseDTO obter(UUID id) {
        return toDto(repository.buscarPorId(tenant(), id).orElseThrow(() -> new NotFoundException("Série não encontrada.")));
    }

    public List<SerieResponseDTO> listarPorAno(UUID idAnoLetivo) {
        UUID tenantId = tenant();
        // garantir que o ano pertence ao tenant
        anoLetivoRepository.buscarPorId(tenantId, idAnoLetivo).orElseThrow(() -> new NotFoundException("Ano letivo não encontrado."));
        return repository.listarPorAnoLetivo(tenantId, idAnoLetivo).stream().map(this::toDto).collect(Collectors.toList());
    }

    public void atualizar(UUID id, AtualizarSerieDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        UUID tenantId = tenant();
        Serie atual = repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Série não encontrada."));
        // checar unicidade caso mude o nome
        if (!atual.getNome().equalsIgnoreCase(dto.getNome()) && repository.existsPorNomeAno(tenantId, atual.getIdAnoLetivo(), dto.getNome()))
            throw new ConflictException("Já existe uma série com este nome neste ano letivo.");
        repository.atualizar(tenantId, id, dto.getNome().trim());
    }

    public void remover(UUID id) {
        repository.remover(tenant(), id);
    }

    private SerieResponseDTO toDto(Serie s) {
        SerieResponseDTO d = new SerieResponseDTO();
        d.setId(s.getId());
        d.setIdAnoLetivo(s.getIdAnoLetivo());
        d.setNome(s.getNome());
        d.setCriadoEm(s.getCriadoEm());
        d.setAtualizadoEm(s.getAtualizadoEm());
        return d;
    }
}
