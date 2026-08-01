package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.*;
import br.com.synge.academico.services.AlunoService;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;

import java.util.UUID;

public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    public void criar(Context ctx) {
        CriarAlunoDTO dto = ctx.bodyAsClass(CriarAlunoDTO.class);
        ctx.status(HttpStatus.CREATED_201).json(service.criar(dto));
    }

    public void atualizar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        AtualizarAlunoDTO dto = ctx.bodyAsClass(AtualizarAlunoDTO.class);
        ctx.json(service.atualizar(id, dto));
    }

    public void obterPorId(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.obterPorId(id));
    }

    public void listar(Context ctx) {
        ctx.json(service.listar());
    }

    public void alterarSituacao(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        AlterarSituacaoAlunoDTO dto = ctx.bodyAsClass(AlterarSituacaoAlunoDTO.class);
        service.alterarSituacao(id, dto);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    public void matricular(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        MatricularAlunoDTO dto = ctx.bodyAsClass(MatricularAlunoDTO.class);
        ctx.status(HttpStatus.CREATED_201).json(service.matricular(id, dto));
    }

    public void transferir(Context ctx) throws Exception {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        TransferirAlunoDTO dto = ctx.bodyAsClass(TransferirAlunoDTO.class);
        service.transferir(id, dto);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    public void adicionarDocumento(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        CriarDocumentoAlunoDTO dto = ctx.bodyAsClass(CriarDocumentoAlunoDTO.class);
        ctx.status(HttpStatus.CREATED_201).json(service.adicionarDocumento(id, dto));
    }

    public void listarDocumentos(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.listarDocumentos(id));
    }

    public void emitirHistoricoEscolar(Context ctx) {
        UUID tenantId = br.com.synge.seguranca.utils.AuthUserContext.getAuthUser().getTenantId();
        UUID idAluno = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.emitirHistoricoEscolar(tenantId, idAluno));
    }
}