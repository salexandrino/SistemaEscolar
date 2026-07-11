package br.com.synge.academico.services;

import br.com.synge.academico.dtos.LancamentoNotaDTO;
import br.com.synge.academico.dtos.AlunoRecuperacaoDTO;
import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Nota;
import br.com.synge.academico.repositories.AvaliacaoRepository;
import br.com.synge.academico.repositories.MatriculaRepository;
import br.com.synge.academico.repositories.NotaRepository;
import br.com.synge.academico.services.media.CalculoMediaPonderada;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LancamentoNotasService {

    private final NotaRepository notaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final MatriculaRepository matriculaRepository;

    private static final BigDecimal MEDIA_MINIMA_APROVACAO = new BigDecimal("6.0");

    public LancamentoNotasService(NotaRepository notaRepository, AvaliacaoRepository avaliacaoRepository, MatriculaRepository matriculaRepository) {
        this.notaRepository = notaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public void lancar(LancamentoNotaDTO dto) {
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) < 0 || dto.getValor().compareTo(BigDecimal.TEN) > 0) {
            throw new ValidationException("A nota deve estar entre 0 e 10.");
        }

        UUID tenantId = tenant();

        Avaliacao av = avaliacaoRepository.buscarPorId(tenantId, dto.getIdAvaliacao())
                .orElseThrow(() -> new NotFoundException("Avaliação não encontrada."));

        if (!matriculaRepository.existeAtiva(tenantId, dto.getIdAluno(), av.idTurma())) {
            throw new ValidationException("Aluno não está matriculado ativamente nesta turma.");
        }

        Nota n = new Nota(
                UUID.randomUUID(),
                tenantId,
                dto.getIdAvaliacao(),
                dto.getIdAluno(),
                dto.getValor(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        notaRepository.salvar(n);
    }

    public List<AlunoRecuperacaoDTO> identificarAlunosEmRecuperacao(UUID tenantId, UUID idTurma, UUID idDisciplina) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.listarPorTurmaEDisciplina(tenantId, idTurma, idDisciplina);
        if (avaliacoes.isEmpty()) return Collections.emptyList();

        List<UUID> alunosIds = matriculaRepository.listarAlunosAtivosPorTurma(tenantId, idTurma);
        List<AlunoRecuperacaoDTO> emRecuperacao = new ArrayList<>();
        CalculoMediaPonderada calculoMedia = new CalculoMediaPonderada();

        for (UUID idAluno : alunosIds) {
            List<UUID> avIds = avaliacoes.stream().map(Avaliacao::id).toList();
            List<Nota> notasAluno = notaRepository.listarPorAvaliacoes(tenantId, avIds, idAluno);

            BigDecimal media = calculoMedia.calcular(notasAluno, avaliacoes);

            if (media.compareTo(MEDIA_MINIMA_APROVACAO) < 0) {
                BigDecimal notaNecessaria = calcularNotaNecessariaRecuperacao(media);
                String nomeAluno = "Aluno " + idAluno.toString().substring(0, 5);

                emRecuperacao.add(new AlunoRecuperacaoDTO(idAluno, nomeAluno, media, notaNecessaria));
            }
        }
        return emRecuperacao;
    }

    public BigDecimal calcularNotaNecessariaRecuperacao(BigDecimal mediaAtual) {
        BigDecimal necessaria = MEDIA_MINIMA_APROVACAO.multiply(new BigDecimal("2")).subtract(mediaAtual);
        return necessaria.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : necessaria.setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> simularNota(BigDecimal mediaAtual, BigDecimal notaHipotetica) {
        BigDecimal novaMedia = mediaAtual.add(notaHipotetica).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        boolean aprovado = novaMedia.compareTo(MEDIA_MINIMA_APROVACAO) >= 0;

        return Map.of("mediaAtual", mediaAtual, "mediaSimulada", novaMedia, "aprovado", aprovado);
    }
}