package br.com.kutuar.academico.services;

import br.com.kutuar.academico.dtos.BoletimDTO;
import br.com.kutuar.academico.models.Avaliacao;
import br.com.kutuar.academico.models.Nota;
import br.com.kutuar.academico.repositories.AvaliacaoRepository;
import br.com.kutuar.academico.repositories.NotaRepository;
import br.com.kutuar.academico.services.media.CalculadoraMediaStrategy;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BoletimService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final CalculadoraMediaStrategy calculadoraMedia;

    public BoletimService(AvaliacaoRepository avaliacaoRepository,
                          NotaRepository notaRepository,
                          CalculadoraMediaStrategy calculadoraMedia) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.calculadoraMedia = calculadoraMedia;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public BoletimDTO gerarBoletim(UUID idAluno, UUID idTurma, UUID idDisciplina) {
        UUID tenantId = tenant();

        List<Avaliacao> avaliacoes = avaliacaoRepository.listarPorTurmaEDisciplina(tenantId, idTurma, idDisciplina);
        // CORRIGIDO: Record usa .id() e não .getId()
        List<UUID> avaliacoesIds = avaliacoes.stream().map(Avaliacao::id).collect(Collectors.toList());
        List<Nota> notas = notaRepository.listarPorAvaliacoes(tenantId, avaliacoesIds, idAluno);

        BigDecimal media = calculadoraMedia.calcular(notas, avaliacoes);

        BoletimDTO b = new BoletimDTO();
        b.setIdAluno(idAluno);
        b.setIdTurma(idTurma);
        b.setIdDisciplina(idDisciplina);
        b.setMedia(media);
        // Frequência ainda não é controlada pelo sistema (não existe tabela de
        // chamada/presença). Deixamos null em vez de 0 para não passar a
        // falsa informação de que o aluno tem 100% de presença.
        b.setTotalPresencas(null);
        b.setTotalFaltas(null);
        b.setTotalFaltasJustificadas(null);

        if (media.compareTo(RegrasAcademicas.MEDIA_MINIMA_APROVACAO) >= 0) {
            // Nota: aprovação aqui considera apenas a média. Frequência mínima
            // (ex.: 75%) não é verificada pois o sistema não tem controle de presença.
            b.setSituacao("APROVADO");
        } else {
            b.setSituacao("REPROVADO");
        }

        return b;
    }


}