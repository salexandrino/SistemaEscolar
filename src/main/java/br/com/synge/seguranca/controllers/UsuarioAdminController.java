package br.com.synge.seguranca.controllers;

import br.com.synge.seguranca.dtos.AtualizarPerfilUsuarioDTO;
import br.com.synge.seguranca.dtos.AtualizarUsuarioDTO;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.exceptions.BusinessException;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.services.UsuarioAdminService;
import br.com.synge.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class UsuarioAdminController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioAdminController.class);
    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    public void listar(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            var usuarios = usuarioAdminService.listar(currentUser);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("total", usuarios.size());
            response.put("usuarios", usuarios.stream().map(this::usuarioToMap).toList());
            ctx.status(HttpStatus.OK);
            ctx.json(response);
        } catch (AuthenticationException | AuthorizationException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Falha ao listar usuarios: {}", e.getMessage());
        } catch (Exception e) {
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao listar usuarios.");
            logger.error("Erro inesperado ao listar usuarios: {}", e.getMessage(), e);
        }
    }

    public void buscarPorId(Context ctx) {
        try {
            UUID id = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();
            Usuario usuario = usuarioAdminService.buscarPorId(id, currentUser);
            ctx.status(HttpStatus.OK);
            ctx.json(usuarioToMap(usuario));
        } catch (AuthenticationException | AuthorizationException | NotFoundException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Falha ao buscar usuario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "ID de usuario invalido.");
            logger.warn("ID de usuario invalido: {}", e.getMessage());
        } catch (Exception e) {
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao buscar usuario.");
            logger.error("Erro inesperado ao buscar usuario: {}", e.getMessage(), e);
        }
    }

    public void atualizar(Context ctx) {
        try {
            UUID id = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();
            AtualizarUsuarioDTO dto = ctx.bodyAsClass(AtualizarUsuarioDTO.class);
            Usuario usuario = usuarioAdminService.atualizar(id, dto, currentUser);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Usuario atualizado com sucesso.");
            response.put("usuario", usuarioToMap(usuario));
            ctx.status(HttpStatus.OK);
            ctx.json(response);
        } catch (AuthenticationException | AuthorizationException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Falha ao atualizar usuario: {}", e.getMessage());
        } catch (ValidationException | NotFoundException | ConflictException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Erro ao atualizar usuario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "ID de usuario invalido.");
            logger.warn("ID de usuario invalido: {}", e.getMessage());
        } catch (Exception e) {
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao atualizar usuario.");
            logger.error("Erro inesperado ao atualizar usuario: {}", e.getMessage(), e);
        }
    }

    public void inativar(Context ctx) {
        try {
            UUID id = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();
            usuarioAdminService.inativar(id, currentUser);
            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Usuario inativado com sucesso."));
        } catch (AuthenticationException | AuthorizationException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Falha ao inativar usuario: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Erro ao inativar usuario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "ID de usuario invalido.");
            logger.warn("ID de usuario invalido: {}", e.getMessage());
        } catch (Exception e) {
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao inativar usuario.");
            logger.error("Erro inesperado ao inativar usuario: {}", e.getMessage(), e);
        }
    }

    public void aprovar(Context ctx) {
        try {
            UUID id = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();
            usuarioAdminService.aprovar(id, currentUser);
            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Usuario aprovado com sucesso."));
        } catch (AuthenticationException | AuthorizationException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Falha ao aprovar usuario: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Erro ao aprovar usuario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "ID de usuario invalido.");
            logger.warn("ID de usuario invalido: {}", e.getMessage());
        } catch (Exception e) {
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao aprovar usuario.");
            logger.error("Erro inesperado ao aprovar usuario: {}", e.getMessage(), e);
        }
    }

    public void alterarPerfil(Context ctx) {
        try {
            UUID id = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();
            AtualizarPerfilUsuarioDTO dto = ctx.bodyAsClass(AtualizarPerfilUsuarioDTO.class);
            usuarioAdminService.alterarPerfil(id, dto, currentUser);
            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Perfil do usuario atualizado com sucesso."));
        } catch (AuthenticationException | AuthorizationException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Falha ao alterar perfil de usuario: {}", e.getMessage());
        } catch (ValidationException | NotFoundException e) {
            responderErro(ctx, e.getStatus(), e.getMessage());
            logger.warn("Erro ao alterar perfil de usuario: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "ID de usuario invalido.");
            logger.warn("ID de usuario invalido: {}", e.getMessage());
        } catch (Exception e) {
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao alterar perfil de usuario.");
            logger.error("Erro inesperado ao alterar perfil de usuario: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> usuarioToMap(Usuario usuario) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", usuario.getId());
        map.put("tenantId", usuario.getTenantId());
        map.put("escolaId", usuario.getEscolaId());
        map.put("nomeCompleto", usuario.getNomeCompleto());
        map.put("email", usuario.getEmail());
        map.put("cpf", usuario.getCpf());
        map.put("telefone", usuario.getTelefone());
        map.put("perfil", usuario.getPerfil());
        map.put("ativo", usuario.isAtivo());
        map.put("bloqueado", usuario.isBloqueado());
        map.put("ultimoLogin", usuario.getUltimoLogin());
        map.put("criadoEm", usuario.getCriadoEm());
        map.put("atualizadoEm", usuario.getAtualizadoEm());
        return map;
    }

    private void responderErro(Context ctx, HttpStatus status, String message) {
        ctx.status(status);
        ctx.json(Map.of("message", message));
    }
}
