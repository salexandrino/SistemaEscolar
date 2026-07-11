package br.com.synge.financeiro.services;

import br.com.synge.financeiro.dtos.CriarParcelamentoDTO;
import br.com.synge.financeiro.models.Mensalidade;
import br.com.synge.financeiro.models.Parcela;
import br.com.synge.financeiro.repositories.MensalidadeRepository;
import br.com.synge.financeiro.repositories.ParcelaRepository;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParcelamentoService {
    private final ParcelaRepository parcelaRepository;
    private final MensalidadeRepository mensalidadeRepository;

    public ParcelamentoService(ParcelaRepository parcelaRepository, MensalidadeRepository mensalidadeRepository) {
        this.parcelaRepository = parcelaRepository;
        this.mensalidadeRepository = mensalidadeRepository;
    }

    public List<Parcela> parcelarMensalidade(UUID tenantId, CriarParcelamentoDTO dto) {
        Mensalidade m = mensalidadeRepository.buscarPorId(tenantId, dto.getIdMensalidade())
                .orElseThrow(() -> new NotFoundException("Mensalidade mãe não encontrada."));

        if (dto.getQuantidadeParcelas() <= 1) {
            throw new ValidationException("A quantidade de parcelas deve ser maior que 1.");
        }

        BigDecimal qtd = new BigDecimal(dto.getQuantidadeParcelas());
        BigDecimal valorCada = m.getValorOriginal().divide(qtd, 2, RoundingMode.HALF_UP);

        List<Parcela> lista = new ArrayList<>();
        for (int i = 1; i <= dto.getQuantidadeParcelas(); i++) {
            Parcela p = new Parcela();
            p.setId(UUID.randomUUID());
            p.setTenantId(tenantId);
            p.setIdMensalidade(m.getId());
            p.setNumeroParcela(i);
            p.setValorParcela(valorCada);
            p.setDataVencimento(m.getDataVencimento().plusMonths(i - 1));
            p.setStatus("PENDENTE");
            lista.add(p);
        }

        parcelaRepository.salvarTodas(lista);
        mensalidadeRepository.atualizarStatus(tenantId, m.getId(), "PARCELADA");
        return lista;
    }
}