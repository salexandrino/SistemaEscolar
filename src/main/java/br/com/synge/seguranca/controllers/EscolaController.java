package br.com.synge.seguranca.controllers;

import br.com.synge.seguranca.dtos.AtualizarEscolaDTO;
import br.com.synge.seguranca.dtos.CriarEscolaDTO;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.exceptions.BusinessException;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.services.EscolaService;
import br.com.synge.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EscolaController {

    private static final Logger logger = LoggerFactory.getLogger(EscolaController.class);
    private final EscolaService escolaService;

    public EscolaController(EscolaService escolaService) {
        this.escolaService = escolaService;
    }

    private Map<String, Object> escolaToMap(Escola escola) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", escola.getId());
        map.put("tenantId", escola.getTenantId());
        map.put("nome", escola.getNome());
        map.put("cnpj", escola.getCnpj());
        map.put("emailInstitucional", escola.getEmailInstitucional());
        map.put("telefone", escola.getTelefone());
        map.put("endereco", escola.getEndereco());
        map.put("numero", escola.getNumero());
        map.put("complemento", escola.getComplemento());
        map.put("bairro", escola.getBairro());
        map.put("cidade", escola.getCidade());
        map.put("estado", escola.getEstado());
        map.put("cep", escola.getCep());
        map.put("nomeResponsavel", escola.getNomeResponsavel());
        map.put("telefoneResponsavel", escola.getTelefoneResponsavel());
        map.put("emailResponsavel", escola.getEmailResponsavel());
        map.put("status", escola.getStatus());
        map.put("criadoEm", escola.getCriadoEm());
        map.put("atualizadoEm", escola.getAtualizadoEm());
        return map;
    }

    private Map<String, Object> escolaToMapSummarized(Escola escola) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", escola.getId());
        map.put("tenantId", escola.getTenantId());
        map.put("nome", escola.getNome());
        map.put("cnpj", escola.getCnpj());
        map.put("status", escola.getStatus());
        map.put("criadoEm", escola.getCriadoEm());
        map.put("atualizadoEm", escola.getAtualizadoEm());
        return map;
    }

    /**
     * POST /escolas - Cadastra uma nova escola
     */
    public void criarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            CriarEscolaDTO dto = ctx.bodyAsClass(CriarEscolaDTO.class);
            Escola escola = escolaService.cadastrarEscola(dto, currentUser);

            ctx.status(HttpStatus.CREATED);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Escola cadastrada com sucesso.");
            response.put("escola", escolaToMapSummarized(escola));
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na criação de escola: {}", e.getMessage());
        } catch (ValidationException | ConflictException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Erro de validação ao criar escola: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao criar escola."));
        }
    }

    /**
     * PATCH /escolas/{id} - Atualiza uma escola
     */
    public void atualizarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            AtualizarEscolaDTO dto = ctx.bodyAsClass(AtualizarEscolaDTO.class);
            Escola escola = escolaService.atualizarEscola(escolaId, dto, currentUser);

            ctx.status(HttpStatus.OK);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Escola atualizada com sucesso.");
            response.put("escola", escolaToMapSummarized(escola));
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na atualização de escola: {}", e.getMessage());
        } catch (ValidationException | NotFoundException | ConflictException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Erro na atualização de escola: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de escola inválido."));
            logger.warn("ID de escola inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao atualizar escola."));
        }
    }

    /**
     * GET /escolas/{id} - Busca uma escola por ID
     */
    public void obterEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.buscarEscolaPorId(escolaId, currentUser);

            ctx.status(HttpStatus.OK);
            ctx.json(escolaToMap(escola));

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao obter escola: {}", e.getMessage());
        } catch (NotFoundException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de escola inválido."));
            logger.warn("ID de escola inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao obter escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao obter escola."));
        }
    }

    /**
     * GET /escolas - Lista todas as escolas
     */
    public void listarEscolas(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            List<Escola> escolas = escolaService.listarTodas(currentUser);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("total", escolas.size());
            response.put("escolas", escolas.stream().map(this::escolaToMapSummarized).toList());

            ctx.status(HttpStatus.OK);
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao listar escolas: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar escolas: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao listar escolas."));
        }
    }

    /**
     * GET /escolas/ativas - Lista apenas as escolas ativas
     */
    public void listarEscolasAtivas(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            List<Escola> escolas = escolaService.listarAtivas(currentUser);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("total", escolas.size());
            response.put("escolas", escolas.stream().map(this::escolaToMapSummarized).toList());

            ctx.status(HttpStatus.OK);
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao listar escolas ativas: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar escolas ativas: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao listar escolas."));
        }
    }

    /**
     * GET /escolas/inativas - Lista apenas as escolas inativas
     */
    public void listarEscolasInativas(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            List<Escola> escolas = escolaService.listarInativas(currentUser);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("total", escolas.size());
            response.put("escolas", escolas.stream().map(this::escolaToMapSummarized).toList());

            ctx.status(HttpStatus.OK);
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao listar escolas inativas: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar escolas inativas: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao listar escolas."));
        }
    }

    /**
     * GET /escolas/cnpj/{cnpj} - Busca uma escola por CNPJ
     */
    public void buscarPorCnpj(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            String cnpj = ctx.pathParam("cnpj");
            Escola escola = escolaService.buscarEscolaPorCnpj(cnpj, currentUser);

            ctx.status(HttpStatus.OK);
            ctx.json(escolaToMap(escola));

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao buscar escola por CNPJ: {}", e.getMessage());
        } catch (NotFoundException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar escola por CNPJ: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao buscar escola."));
        }
    }


    /**
     * PATCH /escolas/{id}/ativar - Ativa uma escola
     */
    public void ativarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.ativarEscola(escolaId, currentUser);

            ctx.status(HttpStatus.OK);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Escola ativada com sucesso.");
            response.put("escola", escolaToMapSummarized(escola));
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na ativação de escola: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Erro na ativação de escola: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de escola inválido."));
            logger.warn("ID de escola inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao ativar escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao ativar escola."));
        }
    }

    /**
     * PATCH /escolas/{id}/inativar - Inativa uma escola
     */
    public void inativarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.inativarEscola(escolaId, currentUser);

            ctx.status(HttpStatus.OK);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Escola inativada com sucesso.");
            response.put("escola", escolaToMapSummarized(escola));
            ctx.json(response);

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na inativação de escola: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Erro na inativação de escola: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de escola inválido."));
            logger.warn("ID de escola inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao inativar escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao inativar escola."));
        }
    }
}
