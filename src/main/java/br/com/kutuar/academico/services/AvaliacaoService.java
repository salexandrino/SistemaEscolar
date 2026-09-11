package br.com.kutuar.academico.services;

import br.com.kutuar.academico.dtos.CriarAvaliacaoDTO;
import br.com.kutuar.academico.models.Avaliacao;
import br.com.kutuar.academico.repositories.AvaliacaoRepository;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AvaliacaoService {
    private final AvaliacaoRepository repo;

    public AvaliacaoService(AvaliacaoRepository repo) { this.repo = repo; }

    public Avaliacao criar(UUID tenantId, CriarAvaliacaoDTO dto) {
        if (!repo.verificarDisciplinaNaMatrizDaTurma(tenantId, dto.getIdTurma(), dto.getIdDisciplina())) {
            throw new ValidationException("A disciplina não pertence à matriz curricular da série desta turma.");
        }

        // Como Avaliacao é um Record, nós a instanciamos de uma vez usando o construtor completo:
        Avaliacao av = new Avaliacao(
                UUID.randomUUID(),           // id
                tenantId,                    // tenantId
                dto.getIdTurma(),            // idTurma
                dto.getIdDisciplina(),       // idDisciplina
                dto.getNome(),               // nome
                dto.getPeso(),               // peso
                LocalDateTime.now(),         // criadoEm
                LocalDateTime.now()          // atualizadoEm
        );

        return repo.criar(av);
    }

    public List<Avaliacao> listar(UUID tenantId, UUID idTurma, UUID idDisciplina) {
        return repo.listarPorTurmaEDisciplina(tenantId, idTurma, idDisciplina);
    }

    public Avaliacao obterPorId(UUID tenantId, UUID id) {
        return repo.buscarPorId(tenantId, id).orElseThrow(() -> new NotFoundException("Avaliação não encontrada."));
    }

    public void atualizar(UUID tenantId, UUID id, CriarAvaliacaoDTO dto) {
        // 1. Obtém a avaliação antiga (que é um Record imutável)
        Avaliacao avAntiga = obterPorId(tenantId, id);

        // 2. Cria uma nova instância de Avaliacao clonando os dados estáveis
        // e injetando as alterações vindas do DTO
        Avaliacao avAtualizada = new Avaliacao(
                avAntiga.id(),               // mantém o mesmo id
                avAntiga.tenantId(),         // mantém o mesmo tenantId
                avAntiga.idTurma(),          // mantém a mesma turma
                avAntiga.idDisciplina(),     // mantém a mesma disciplina
                dto.getNome(),               // NOVO nome atualizado do DTO
                dto.getPeso(),               // NOVO peso atualizado do DTO
                avAntiga.criadoEm(),         // mantém a data de criação original
                LocalDateTime.now()          // atualiza o campo atualizadoEm para o momento atual
        );

        // 3. Passa o novo Record modificado para o repositório persistir no banco
        repo.atualizar(avAtualizada);
    }

    public void remover(UUID tenantId, UUID id) {
        repo.remover(tenantId, id);
    }
}