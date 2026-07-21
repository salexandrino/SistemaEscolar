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
        BigDecimal valorCada = m.getValorOriginal().divide(qtd, 2, RoundingMode.DOWN);

        // Arredondar cada parcela para baixo e jogar a diferença (os centavos
        // que sobram) na última parcela garante que a SOMA das parcelas seja
        // sempre exatamente igual ao valor original da mensalidade.
        // Ex.: R$100,00 em 3x -> 33,33 + 33,33 + 33,34 = 100,00 (antes: 99,99).
        BigDecimal somaParcelasIniciais = valorCada.multiply(qtd.subtract(BigDecimal.ONE));
        BigDecimal valorUltimaParcela = m.getValorOriginal().subtract(somaParcelasIniciais);

        List<Parcela> lista = new ArrayList<>();
        for (int i = 1; i <= dto.getQuantidadeParcelas(); i++) {
            Parcela p = new Parcela();
            p.setId(UUID.randomUUID());
            p.setTenantId(tenantId);
            p.setIdMensalidade(m.getId());
            p.setNumeroParcela(i);
            p.setValorParcela(i == dto.getQuantidadeParcelas() ? valorUltimaParcela : valorCada);
            p.setDataVencimento(m.getDataVencimento().plusMonths(i - 1));
            p.setStatus("PENDENTE");
            lista.add(p);
        }

        parcelaRepository.salvarTodas(lista);
        mensalidadeRepository.atualizarStatus(tenantId, m.getId(), "PARCELADA");
        return lista;
    }
}