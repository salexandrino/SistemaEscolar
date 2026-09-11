package br.com.kutuar.financeiro.services;

import br.com.kutuar.financeiro.models.Mensalidade;
import br.com.kutuar.financeiro.repositories.MensalidadeRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlertaService {
    private final MensalidadeRepository mensalidadeRepository;
    private final InadimplenciaService inadimplenciaService;

    public AlertaService(MensalidadeRepository mensalidadeRepository, InadimplenciaService inadimplenciaService) {
        this.mensalidadeRepository = mensalidadeRepository;
        this.inadimplenciaService = inadimplenciaService;
    }

    public Map<String, Object> gerarAlertasFinanceiros(UUID tenantId) {
        // A transição PENDENTE -> EM_ATRASO fica centralizada no
        // InadimplenciaService, para não termos duas implementações
        // independentes da mesma regra que podem divergir com o tempo.
        List<Mensalidade> atrasadas = inadimplenciaService.listarDevedores(tenantId);

        List<Map<String, Object>> alertasAVencer = new ArrayList<>();
        List<Map<String, Object>> alertasVencidos = new ArrayList<>();

        LocalDate hoje = LocalDate.now();

        // 1. Identificar mensalidades a vencer nos próximos 5 dias
        // (após a chamada acima, as que já venceram deixaram de estar PENDENTE)
        List<Mensalidade> pendentes = mensalidadeRepository.listarPorStatus(tenantId, "PENDENTE");
        for (Mensalidade m : pendentes) {
            long diasParaVencer = ChronoUnit.DAYS.between(hoje, m.getDataVencimento());
            if (diasParaVencer >= 0 && diasParaVencer <= 5) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("idMensalidade", m.getId());
                alerta.put("idAluno", m.getIdAluno());
                alerta.put("valor", m.getValorOriginal());
                alerta.put("vencimento", m.getDataVencimento());
                alerta.put("mensagem", "Mensalidade vence em " + diasParaVencer + " dias.");
                alertasAVencer.add(alerta);
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