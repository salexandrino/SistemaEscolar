package br.com.synge.financeiro.services;

import br.com.synge.financeiro.models.Mensalidade;
import br.com.synge.financeiro.repositories.MensalidadeRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RelatorioFinanceiroService {
    private final MensalidadeRepository mensalidadeRepository;

    public RelatorioFinanceiroService(MensalidadeRepository mensalidadeRepository) {
        this.mensalidadeRepository = mensalidadeRepository;
    }

    public Map<String, Object> gerarPrevisaoEFluxo(UUID tenantId) {
        List<Mensalidade> pagas = mensalidadeRepository.listarPorStatus(tenantId, "PAGA");
        List<Mensalidade> pendentes = mensalidadeRepository.listarPorStatus(tenantId, "PENDENTE");
        List<Mensalidade> atrasadas = mensalidadeRepository.listarPorStatus(tenantId, "EM_ATRASO");

        BigDecimal totalRecebido = pagas.stream().map(Mensalidade::getValorOriginal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrevisto = pendentes.stream().map(Mensalidade::getValorOriginal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAtrasado = atrasadas.stream().map(Mensalidade::getValorOriginal).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("receitaRecebida", totalRecebido);
        relatorio.put("receitaPendente", totalPrevisto);
        relatorio.put("receitaAtrasada", totalAtrasado);
        relatorio.put("faturamentoGlobalPrevisto", totalRecebido.add(totalPrevisto).add(totalAtrasado));

        return relatorio;
    }
}