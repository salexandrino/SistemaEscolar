package br.com.kutuar.academico.services;

import br.com.kutuar.academico.dtos.AtualizarDisciplinaDTO;
import br.com.kutuar.academico.dtos.CriarDisciplinaDTO;
import br.com.kutuar.academico.models.Disciplina;
import br.com.kutuar.academico.repositories.DisciplinaRepository;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;

import java.util.List;
import java.util.UUID;

public class DisciplinaService {

    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
    }

    public Disciplina criar(UUID tenantId, CriarDisciplinaDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            throw new ValidationException("Nome da disciplina é obrigatório.");
        }
        String nome = dto.getNome().trim();
        if (nome.length() > 160) {
            throw new ValidationException("Nome da disciplina deve ter no máximo 160 caracteres.");
        }
        if (repository.existsByNome(tenantId, nome)) {
            throw new ConflictException("Já existe uma disciplina com este nome.");
        }
        return repository.criar(tenantId, nome);
    }

    public Disciplina atualizar(UUID tenantId, UUID id, AtualizarDisciplinaDTO dto) {
        if (id == null) throw new ValidationException("ID inválido.");
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            throw new ValidationException("Nome da disciplina é obrigatório.");
        }
        var existente = repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Disciplina não encontrada."));
        String novoNome = dto.getNome().trim();
        if (!existente.getNome().equalsIgnoreCase(novoNome) && repository.existsByNome(tenantId, novoNome)) {
            throw new ConflictException("Já existe uma disciplina com este nome.");
        }
        repository.atualizar(tenantId, id, novoNome);
        return repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Disciplina não encontrada após atualização."));
    }

    public Disciplina obter(UUID tenantId, UUID id) {
        return repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Disciplina não encontrada."));
    }

    public List<Disciplina> listar(UUID tenantId) {
        return repository.listar(tenantId);
    }

    public void remover(UUID tenantId, UUID id) {
        // garante existência para mensagem consistente
        repository.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Disciplina não encontrada."));
        repository.remover(tenantId, id);
    }
}
