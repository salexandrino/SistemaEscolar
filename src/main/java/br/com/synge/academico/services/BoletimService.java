package br.com.synge.academico.services;

import br.com.synge.academico.dtos.BoletimDTO;
import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Frequencia;
import br.com.synge.academico.models.Nota;
import br.com.synge.academico.repositories.AvaliacaoRepository;
import br.com.synge.academico.repositories.FrequenciaRepository;
import br.com.synge.academico.repositories.NotaRepository;
import br.com.synge.academico.services.media.CalculadoraMediaStrategy;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BoletimService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final CalculadoraMediaStrategy calculadoraMedia;

    public BoletimService(AvaliacaoRepository avaliacaoRepository, 
                          NotaRepository notaRepository, 
                          FrequenciaRepository frequenciaRepository,
                          CalculadoraMediaStrategy calculadoraMedia) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.frequenciaRepository = frequenciaRepository;
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
        List<UUID> avaliacoesIds = avaliacoes.stream().map(Avaliacao::getId).collect(Collectors.toList());
        List<Nota> notas = notaRepository.listarPorAvaliacoes(tenantId, avaliacoesIds, idAluno);
        List<Frequencia> frequencias = frequenciaRepository.listarPorAlunoEDisciplina(tenantId, idAluno, idTurma, idDisciplina);

        BigDecimal media = calculadoraMedia.calcular(notas, avaliacoes);

        long presencas = frequencias.stream().filter(f -> "PRESENTE".equals(f.getSituacao())).count();
        long faltas = frequencias.stream().filter(f -> "FALTA".equals(f.getSituacao())).count();
        long faltasJustificadas = frequencias.stream().filter(f -> "FALTA_JUSTIFICADA".equals(f.getSituacao())).count();

        BoletimDTO b = new BoletimDTO();
        b.setIdAluno(idAluno);
        b.setIdTurma(idTurma);
        b.setIdDisciplina(idDisciplina);
        b.setMedia(media);
        b.setTotalPresencas((int) presencas);
        b.setTotalFaltas((int) faltas);
        b.setTotalFaltasJustificadas((int) faltasJustificadas);

        if (media.compareTo(new BigDecimal("7.0")) >= 0 && faltas <= 10) { // Regra fictícia simples
            b.setSituacao("APROVADO");
        } else {
            b.setSituacao("REPROVADO");
        }

        return b;
    }
}
