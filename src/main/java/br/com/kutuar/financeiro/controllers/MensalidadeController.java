package br.com.kutuar.financeiro.controllers;

import br.com.kutuar.financeiro.dtos.CriarMensalidadeDTO;
import br.com.kutuar.financeiro.dtos.CriarDescontoDTO;
import br.com.kutuar.financeiro.dtos.RegistrarPagamentoDTO;
import br.com.kutuar.financeiro.dtos.CriarParcelamentoDTO;
import br.com.kutuar.financeiro.services.MensalidadeService;
import br.com.kutuar.financeiro.services.ParcelamentoService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import java.util.Map;
import java.util.UUID;

public class MensalidadeController {
    private final MensalidadeService mensalidadeService;
    private final ParcelamentoService parcelamentoService;

    public MensalidadeController(MensalidadeService mensalidadeService, ParcelamentoService parcelamentoService) {
        this.mensalidadeService = mensalidadeService;
        this.parcelamentoService = parcelamentoService;
    }

    public void cadastrar(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        CriarMensalidadeDTO dto = ctx.bodyAsClass(CriarMensalidadeDTO.class);
        ctx.status(201).json(mensalidadeService.cadastrar(tenantId, dto));
    }

    public void aplicarDesconto(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        CriarDescontoDTO dto = ctx.bodyAsClass(CriarDescontoDTO.class);
        mensalidadeService.aplicarDesconto(tenantId, dto);
        ctx.status(200).json(Map.of("message", "Desconto aplicado com sucesso."));
    }

    public void registrarPagamento(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        RegistrarPagamentoDTO dto = ctx.bodyAsClass(RegistrarPagamentoDTO.class);
        mensalidadeService.registrarPagamento(tenantId, dto);
        ctx.status(200).json(Map.of("message", "Pagamento registrado com sucesso."));
    }

    public void parcelar(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        CriarParcelamentoDTO dto = ctx.bodyAsClass(CriarParcelamentoDTO.class);
        ctx.status(201).json(parcelamentoService.parcelarMensalidade(tenantId, dto));
    }

    public void listarPorAluno(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        UUID idAluno = UUID.fromString(ctx.pathParam("idAluno"));
        ctx.json(mensalidadeService.listarPorAluno(tenantId, idAluno));
    }
}