package br.com.synge.academico.services;

import br.com.synge.academico.dtos.AnoLetivoResponseDTO;
import br.com.synge.academico.dtos.CriarAnoLetivoDTO;
import br.com.synge.academico.models.AnoLetivo;
import br.com.synge.academico.repositories.AnoLetivoRepository;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnoLetivoService {

    private final AnoLetivoRepository repository;
    private final AnoLetivoCloneRepository cloneRepository;

    public AnoLetivoService(AnoLetivoRepository repository, AnoLetivoCloneRepository cloneRepository) {
        this.repository = repository;
        this.cloneRepository = cloneRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public AnoLetivoResponseDTO criar(CriarAnoLetivoDTO dto) {
        if (dto == null || dto.getAno() == null) throw new ValidationException("Ano é obrigatório.");
        UUID tenantId = tenant();
        int ano = dto.getAno();
        if (repository.existsPorAno(tenantId, ano)) throw new ConflictException("Ano letivo já existe para este tenant.");

        // Datas padrão conforme alinhado: 01/02 a 15/12 do mesmo ano
        LocalDate inicio = LocalDate.of(ano, 2, 1);
        LocalDate fim = LocalDate.of(ano, 12, 15);
        if (fim.isBefore(inicio)) throw new ValidationException("Período do ano letivo inválido.");

        AnoLetivo criado = repository.criar(tenantId, ano, inicio, fim);
        return toDto(criado);
    }

    public void arquivar(UUID id) {
        UUID tenantId = tenant();
        AnoLetivo a = repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Ano letivo não encontrado."));
        if ("ARQUIVADO".equalsIgnoreCase(a.getSituacao())) {
            throw new ValidationException("Ano letivo já está arquivado.");
        }
        repository.arquivar(tenantId, id);
    }

    public void definirAtivo(UUID id) {
        UUID tenantId = tenant();
        repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Ano letivo não encontrado."));
        repository.definirAtivoUnico(tenantId, id);
    }

    public List<AnoLetivoResponseDTO> listar() {
        UUID tenantId = tenant();
        return repository.listar(tenantId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<AnoLetivoResponseDTO> historicoAnosAnteriores(int anoAtual) {
        UUID tenantId = tenant();
        return repository.listarAnteriores(tenantId, anoAtual).stream().map(this::toDto).collect(Collectors.toList());
    }

    public void clonarConfiguracoes(UUID idOrigem, UUID idDestino) {
        UUID tenantId = tenant();
        repository.buscarPorId(tenantId, idOrigem).orElseThrow(() -> new NotFoundException("Ano letivo de origem não encontrado."));
        repository.buscarPorId(tenantId, idDestino).orElseThrow(() -> new NotFoundException("Ano letivo de destino não encontrado."));

        try {
            cloneRepository.clonarAnoLetivo(tenantId, idOrigem, idDestino);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao clonar configurações: " + e.getMessage(), e);
        }
    }

    private AnoLetivoResponseDTO toDto(AnoLetivo a) {
        AnoLetivoResponseDTO d = new AnoLetivoResponseDTO();
        d.setId(a.getId());
        d.setAno(a.getAno());
        d.setDataInicio(a.getDataInicio());
        d.setDataFim(a.getDataFim());
        d.setSituacao(a.getSituacao());
        d.setAtivo(a.isAtivo());
        d.setCriadoEm(a.getCriadoEm());
        d.setAtualizadoEm(a.getAtualizadoEm());
        return d;
    }
}
