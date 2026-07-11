package br.com.synge.financeiro.services;

import br.com.synge.financeiro.dtos.CriarMensalidadeDTO;
import br.com.synge.financeiro.dtos.CriarDescontoDTO;
import br.com.synge.financeiro.dtos.RegistrarPagamentoDTO;
import br.com.synge.financeiro.models.Mensalidade;
import br.com.synge.financeiro.models.Desconto;
import br.com.synge.financeiro.models.Pagamento;
import br.com.synge.financeiro.repositories.MensalidadeRepository;
import br.com.synge.financeiro.repositories.DescontoRepository;
import br.com.synge.financeiro.repositories.PagamentoRepository;
import br.com.synge.financeiro.strategies.EstrategiaMultaJuros;
import br.com.synge.financeiro.strategies.JurosDiarioStrategy;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MensalidadeService {
    private final MensalidadeRepository repository;
    private final DescontoRepository descontoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EstrategiaMultaJuros estrategiaMultaJuros;

    public MensalidadeService(MensalidadeRepository repository, DescontoRepository descontoRepository, PagamentoRepository pagamentoRepository) {
        this.repository = repository;
        this.descontoRepository = descontoRepository;
        this.pagamentoRepository = pagamentoRepository;
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

        // Calcula acréscimos se estiver atrasada usando a Strategy
        BigDecimal acrescimo = estrategiaMultaJuros.calcularAcrescimo(m.getValorOriginal(), m.getDataVencimento(), LocalDate.now());
        BigDecimal totalEsperado = m.getValorOriginal().add(acrescimo);

        if (dto.getValorPago().compareTo(totalEsperado) < 0) {
            throw new ValidationException("Valor insuficiente para quitar a dívida líquida calculada: R$ " + totalEsperado);
        }

        Pagamento p = new Pagamento();
        p.setId(UUID.randomUUID());
        p.setTenantId(tenantId);
        p.setIdMensalidade(dto.getIdMensalidade());
        p.setIdParcela(dto.getIdParcela());
        p.setValorPago(dto.getValorPago());
        p.setDataPagamento(LocalDateTime.now());
        p.setFormaPagamento(dto.getFormaPagamento());
        pagamentoRepository.registrar(p);

        repository.atualizarStatus(tenantId, m.getId(), "PAGA");
    }

    public List<Mensalidade> listarPorAluno(UUID tenantId, UUID idAluno) {
        return repository.listarPorAluno(tenantId, idAluno);
    }
}