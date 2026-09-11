package br.com.kutuar.financeiro.services;

import br.com.kutuar.financeiro.models.Mensalidade;
import br.com.kutuar.financeiro.repositories.MensalidadeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InadimplenciaService {
    private final MensalidadeRepository mensalidadeRepository;

    public InadimplenciaService(MensalidadeRepository mensalidadeRepository) {
        this.mensalidadeRepository = mensalidadeRepository;
    }

    public List<Mensalidade> listarDevedores(UUID tenantId) {
        List<Mensalidade> pendentes = mensalidadeRepository.listarPorStatus(tenantId, "PENDENTE");
        List<Mensalidade> devedores = new ArrayList<>();

        for (Mensalidade m : pendentes) {
            if (m.getDataVencimento().isBefore(LocalDate.now())) {
                // Atualiza o status em tempo de execução caso já tenha estourado a data
                mensalidadeRepository.atualizarStatus(tenantId, m.getId(), "EM_ATRASO");
                m.setStatus("EM_ATRASO");
                devedores.add(m);
            }
        }

        // Junta com aquelas que já estavam marcadas como EM_ATRASO definitivamente
        devedores.addAll(mensalidadeRepository.listarPorStatus(tenantId, "EM_ATRASO"));
        return devedores;
    }
}