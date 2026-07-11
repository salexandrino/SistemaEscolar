package br.com.synge.financeiro.services;

import br.com.synge.financeiro.models.Mensalidade;
import br.com.synge.financeiro.repositories.MensalidadeRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlertaService {
    private final MensalidadeRepository mensalidadeRepository;

    public AlertaService(MensalidadeRepository mensalidadeRepository) {
        this.mensalidadeRepository = mensalidadeRepository;
    }

    public Map<String, Object> gerarAlertasFinanceiros(UUID tenantId) {
        List<Mensalidade> pendentes = mensalidadeRepository.listarPorStatus(tenantId, "PENDENTE");
        List<Mensalidade> atrasadas = mensalidadeRepository.listarPorStatus(tenantId, "EM_ATRASO");

        List<Map<String, Object>> alertasAVencer = new ArrayList<>();
        List<Map<String, Object>> alertasVencidos = new ArrayList<>();

        LocalDate hoje = LocalDate.now();

        // 1. Identificar mensalidades a vencer nos próximos 5 dias
        for (Mensalidade m : pendentes) {
            if (m.getDataVencimento().isAfter(hoje) || m.getDataVencimento().isEqual(hoje)) {
                long diasParaVencer = ChronoUnit.DAYS.between(hoje, m.getDataVencimento());
                if (diasParaVencer <= 5) {
                    Map<String, Object> alerta = new HashMap<>();
                    alerta.put("idMensalidade", m.getId());
                    alerta.put("idAluno", m.getIdAluno());
                    alerta.put("valor", m.getValorOriginal());
                    alerta.put("vencimento", m.getDataVencimento());
                    alerta.put("mensagem", "Mensalidade vence em " + diasParaVencer + " dias.");
                    alertasAVencer.add(alerta);
                }
            } else {
                // Caso esteja pendente mas a data passou, ela deveria estar como EM_ATRASO
                m.setStatus("EM_ATRASO");
                mensalidadeRepository.atualizarStatus(tenantId, m.getId(), "EM_ATRASO");
                atrasadas.add(m);
            }
        }

        // 2. Agregar mensalidades vencidas (Inadimplência acumulada)
        for (Mensalidade m : atrasadas) {
            long diasAtraso = ChronoUnit.DAYS.between(m.getDataVencimento(), hoje);
            Map<String, Object> alerta = new HashMap<>();
            alerta.put("idMensalidade", m.getId());
            alerta.put("idAluno", m.getIdAluno());
            alerta.put("valor", m.getValorOriginal());
            alerta.put("vencimento", m.getDataVencimento());
            alerta.put("mensagem", "Mensalidade em atraso há " + diasAtraso + " dias.");
            alertasVencidos.add(alerta);
        }

        Map<String, Object> painelAlertas = new HashMap<>();
        painelAlertas.put("aVencer", alertasAVencer);
        painelAlertas.put("vencidos", alertasVencidos);
        painelAlertas.put("totalAlertasAtivos", alertasAVencer.size() + alertasVencidos.size());

        return painelAlertas;
    }
}