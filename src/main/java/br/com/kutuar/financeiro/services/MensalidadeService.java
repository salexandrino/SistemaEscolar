package br.com.kutuar.financeiro.services;

import br.com.kutuar.financeiro.dtos.CriarMensalidadeDTO;
import br.com.kutuar.financeiro.dtos.CriarDescontoDTO;
import br.com.kutuar.financeiro.dtos.RegistrarPagamentoDTO;
import br.com.kutuar.financeiro.models.Mensalidade;
import br.com.kutuar.financeiro.models.Desconto;
import br.com.kutuar.financeiro.models.Pagamento;
import br.com.kutuar.financeiro.models.Parcela;
import br.com.kutuar.financeiro.repositories.MensalidadeRepository;
import br.com.kutuar.financeiro.repositories.DescontoRepository;
import br.com.kutuar.financeiro.repositories.PagamentoRepository;
import br.com.kutuar.financeiro.repositories.ParcelaRepository;
import br.com.kutuar.financeiro.strategies.EstrategiaMultaJuros;
import br.com.kutuar.financeiro.strategies.JurosDiarioStrategy;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MensalidadeService {
    private final MensalidadeRepository repository;
    private final DescontoRepository descontoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final EstrategiaMultaJuros estrategiaMultaJuros;

    public MensalidadeService(MensalidadeRepository repository, DescontoRepository descontoRepository,
                              PagamentoRepository pagamentoRepository, ParcelaRepository parcelaRepository) {
        this.repository = repository;
        this.descontoRepository = descontoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        // Injeta por padrão a estratégia de juros diários
        this.estrategiaMultaJuros = new JurosDiarioStrategy();
    }

    public Mensalidade cadastrar(UUID tenantId, CriarMensalidadeDTO dto) {
        if (dto.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("O valor da mensalidade deve ser maior que zero.");
        }
        Mensalidade m = new Mensalidade();
        m.setId(UUID.randomUUID());
        m.setTenantId(tenantId);
        m.setIdAluno(dto.getIdAluno());
        m.setValorOriginal(dto.getValorOriginal());
        m.setDataVencimento(dto.getDataVencimento());
        m.setStatus("PENDENTE");
        m.setCriadoEm(LocalDateTime.now());
        m.setAtualizadoEm(LocalDateTime.now());
        return repository.criar(m);
    }

    public void aplicarDesconto(UUID tenantId, CriarDescontoDTO dto) {
        Mensalidade m = repository.buscarPorId(tenantId, dto.getIdMensalidade())
                .orElseThrow(() -> new NotFoundException("Mensalidade não encontrada."));

        if (!"PENDENTE".equals(m.getStatus())) {
            throw new ValidationException("Só é possível aplicar descontos em parcelas PENDENTES.");
        }
        if (dto.getValorDesconto().compareTo(m.getValorOriginal()) >= 0) {
            throw new ValidationException("O desconto não pode ser maior ou igual ao valor da mensalidade.");
        }

        Desconto d = new Desconto();
        d.setId(UUID.randomUUID());
        d.setTenantId(tenantId);
        d.setIdMensalidade(dto.getIdMensalidade());
        d.setMotivo(dto.getMotivo());
        d.setValorDesconto(dto.getValorDesconto());
        descontoRepository.aplicarDesconto(d);
    }

    public void registrarPagamento(UUID tenantId, RegistrarPagamentoDTO dto) {
        Mensalidade m = repository.buscarPorId(tenantId, dto.getIdMensalidade())
                .orElseThrow(() -> new NotFoundException("Mensalidade não encontrada."));

        if ("PAGA".equals(m.getStatus())) {
            throw new ValidationException("Esta mensalidade já consta como paga.");
        }

        if (dto.getIdParcela() != null) {
            registrarPagamentoDeParcela(tenantId, m, dto);
        } else {
            registrarPagamentoIntegral(tenantId, m, dto);
        }
    }

    /**
     * Mensalidade paga de uma vez (sem parcelamento): aplica os descontos
     * já concedidos antes de calcular multa/juros sobre o saldo devedor.
     */
    private void registrarPagamentoIntegral(UUID tenantId, Mensalidade m, RegistrarPagamentoDTO dto) {
        BigDecimal desconto = descontoRepository.somarDescontosPorMensalidade(tenantId, m.getId());
        BigDecimal valorLiquido = m.getValorOriginal().subtract(desconto);
        if (valorLiquido.compareTo(BigDecimal.ZERO) < 0) {
            valorLiquido = BigDecimal.ZERO;
        }

        BigDecimal acrescimo = estrategiaMultaJuros.calcularAcrescimo(valorLiquido, m.getDataVencimento(), LocalDate.now());
        BigDecimal totalEsperado = valorLiquido.add(acrescimo);

        if (dto.getValorPago().compareTo(totalEsperado) < 0) {
            throw new ValidationException("Valor insuficiente para quitar a dívida líquida calculada: R$ " + totalEsperado);
        }

        salvarPagamento(tenantId, dto);
        repository.atualizarStatus(tenantId, m.getId(), "PAGA");
    }

    /**
     * Pagamento de uma parcela específica: usa o valor e o vencimento da
     * PRÓPRIA parcela (não da mensalidade "mãe") para calcular multa/juros,
     * e só marca a mensalidade como PAGA quando todas as parcelas quitarem.
     */
    private void registrarPagamentoDeParcela(UUID tenantId, Mensalidade m, RegistrarPagamentoDTO dto) {
        Parcela parcela = parcelaRepository.buscarPorId(tenantId, dto.getIdParcela())
                .orElseThrow(() -> new NotFoundException("Parcela não encontrada."));

        if (!parcela.getIdMensalidade().equals(m.getId())) {
            throw new ValidationException("A parcela informada não pertence à mensalidade informada.");
        }
        if ("PAGA".equals(parcela.getStatus())) {
            throw new ValidationException("Esta parcela já consta como paga.");
        }

        BigDecimal acrescimo = estrategiaMultaJuros.calcularAcrescimo(parcela.getValorParcela(), parcela.getDataVencimento(), LocalDate.now());
        BigDecimal totalEsperado = parcela.getValorParcela().add(acrescimo);

        if (dto.getValorPago().compareTo(totalEsperado) < 0) {
            throw new ValidationException("Valor insuficiente para quitar a parcela: R$ " + totalEsperado);
        }

        salvarPagamento(tenantId, dto);
        parcelaRepository.atualizarStatus(tenantId, parcela.getId(), "PAGA");

        boolean todasPagas = parcelaRepository.listarPorMensalidade(tenantId, m.getId()).stream()
                .allMatch(p -> "PAGA".equals(p.getStatus()));
        if (todasPagas) {
            repository.atualizarStatus(tenantId, m.getId(), "PAGA");
        }
    }

    private void salvarPagamento(UUID tenantId, RegistrarPagamentoDTO dto) {
        Pagamento p = new Pagamento();
        p.setId(UUID.randomUUID());
        p.setTenantId(tenantId);
        p.setIdMensalidade(dto.getIdMensalidade());
        p.setIdParcela(dto.getIdParcela());
        p.setValorPago(dto.getValorPago());
        p.setDataPagamento(LocalDateTime.now());
        p.setFormaPagamento(dto.getFormaPagamento());
        pagamentoRepository.registrar(p);
    }

    public List<Mensalidade> listarPorAluno(UUID tenantId, UUID idAluno) {
        return repository.listarPorAluno(tenantId, idAluno);
    }
}