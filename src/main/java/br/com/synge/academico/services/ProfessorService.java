package br.com.synge.academico.services;

import br.com.synge.academico.dtos.*;
import br.com.synge.academico.models.Professor;
import br.com.synge.academico.repositories.ProfessorRepository;
import br.com.synge.academico.repositories.TurmaDisciplinaProfessorRepository;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.strategies.ValidadorCpf;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfessorService {

    private final ProfessorRepository repository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;
    private final ValidadorCpf validadorCpf;

    public ProfessorService(ProfessorRepository repository, TurmaDisciplinaProfessorRepository tdpRepository, ValidadorCpf validadorCpf) {
        this.repository = repository;
        this.tdpRepository = tdpRepository;
        this.validadorCpf = validadorCpf;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public ProfessorResponseDTO criar(CriarProfessorDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        if (dto.getCpf() == null || dto.getCpf().isBlank()) throw new ValidationException("CPF é obrigatório.");
        
        validadorCpf.validar(dto.getCpf());
        
        UUID tenantId = tenant();
        if (repository.existsByCpf(tenantId, dto.getCpf())) {
            throw new ConflictException("CPF já cadastrado para este tenant.");
        }

        Professor p = new Professor();
        p.setId(UUID.randomUUID());
        p.setTenantId(tenantId);
        p.setNome(dto.getNome().trim());
        p.setCpf(dto.getCpf().trim());
        p.setEmail(dto.getEmail());
        p.setTelefone(dto.getTelefone());
        p.setCargaHorariaContratual(dto.getCargaHorariaContratual() != null ? dto.getCargaHorariaContratual() : 20);
        p.setAtivo(true);
        p.setCriadoEm(LocalDateTime.now());
        p.setAtualizadoEm(LocalDateTime.now());

        Professor criado = repository.criar(p);
        return toDto(criado);
    }

    public ProfessorResponseDTO atualizar(UUID id, AtualizarProfessorDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) throw new ValidationException("Nome é obrigatório.");
        UUID tenantId = tenant();
        
        Professor p = repository.buscarPorId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Professor não encontrado."));
        
        p.setNome(dto.getNome().trim());
        p.setEmail(dto.getEmail());
        p.setTelefone(dto.getTelefone());
        if (dto.getCargaHorariaContratual() != null) {
            p.setCargaHorariaContratual(dto.getCargaHorariaContratual());
        }
        
        repository.atualizar(p);
        
        return toDto(repository.buscarPorId(tenantId, id).get());
    }

    public void inativar(UUID id) {
        UUID tenantId = tenant();
        repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Professor não encontrado."));
        repository.inativar(tenantId, id);
    }

    public ProfessorResponseDTO obterPorId(UUID id) {
        UUID tenantId = tenant();
        Professor p = repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Professor não encontrado."));
        return toDto(p);
    }

    public List<ProfessorResponseDTO> listar() {
        UUID tenantId = tenant();
        return repository.listar(tenantId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public GradeProfessorDTO consultarGrade(UUID idProfessor) {
        UUID tenantId = tenant();
        Professor p = repository.buscarPorId(tenantId, idProfessor)
                .orElseThrow(() -> new NotFoundException("Professor não encontrado."));
        
        List<GradeProfessorItemDTO> itens = tdpRepository.consultarGradeProfessor(tenantId, idProfessor);
        int cargaAtual = tdpRepository.somatorioCargaHorariaProfessor(tenantId, idProfessor);
        // converter para anual (40 semanas)
        int cargaContratualAnual = p.getCargaHorariaContratual() * 40; 
        
        GradeProfessorDTO dto = new GradeProfessorDTO();
        dto.setCargaHorariaTotal(cargaAtual);
        dto.setCargaHorariaContratual(cargaContratualAnual);
        dto.setItens(itens);
        return dto;
    }

    private ProfessorResponseDTO toDto(Professor p) {
        ProfessorResponseDTO dto = new ProfessorResponseDTO();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setCpf(p.getCpf());
        dto.setEmail(p.getEmail());
        dto.setTelefone(p.getTelefone());
        dto.setCargaHorariaContratual(p.getCargaHorariaContratual());
        dto.setAtivo(p.isAtivo());
        dto.setCriadoEm(p.getCriadoEm());
        return dto;
    }
}
