package br.com.synge.administrativo.services;

import br.com.synge.administrativo.models.Auditoria;
import br.com.synge.administrativo.repositories.AuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaService.class);
    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public Auditoria registrarAcao(UUID escolaId, UUID usuarioId, String tipoAcao, String detalhes) {
        Auditoria auditoria = new Auditoria();
        auditoria.setEscolaId(escolaId);
        auditoria.setUsuarioId(usuarioId);
        auditoria.setTipoAcao(tipoAcao);
        auditoria.setDetalhes(detalhes);
        auditoria.setDataAcao(LocalDateTime.now());
        Auditoria savedAuditoria = repository.save(auditoria);
        logger.info("Ação de auditoria registrada: Tipo={}, Usuário={}, Detalhes={}", tipoAcao, usuarioId, detalhes);
        return savedAuditoria;
    }

    public List<Auditoria> findByEscolaId(UUID escolaId) {
        return repository.findByEscolaId(escolaId);
    }
}
