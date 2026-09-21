package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.AtualizarEscolaDTO;
import br.com.kutuar.seguranca.dtos.CriarEscolaDTO;
import br.com.kutuar.seguranca.dtos.CriarEscolaResponseDTO;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.enums.EscolaStatus;
import br.com.kutuar.seguranca.dtos.PageResponse;
import br.com.kutuar.seguranca.dtos.EscolaResumoDTO;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
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
     * GET /escolas/{id} - Busca uma escola por ID
     */
    public void obterEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID schoolId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.buscarEscolaPorId(schoolId, currentUser);

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
     * POST /escolas - Cadastra uma nova escola
     */
    public void criarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            CriarEscolaDTO dto = new CriarEscolaDTO();
            dto.setNome(ctx.formParam("nome"));
            dto.setCnpj(ctx.formParam("cnpj"));
            dto.setEmailInstitucional(ctx.formParam("emailInstitucional"));
            dto.setTelefone(ctx.formParam("telefone"));
            dto.setEndereco(ctx.formParam("endereco"));
            dto.setBairro(ctx.formParam("bairro"));
            dto.setCep(ctx.formParam("cep"));
            dto.setCidade(ctx.formParam("cidade"));
            dto.setEstado(ctx.formParam("estado"));
            dto.setNomeResponsavel(ctx.formParam("nomeResponsavel"));
            dto.setCpfResponsavel(ctx.formParam("cpfResponsavel"));
            dto.setTelefoneResponsavel(ctx.formParam("telefoneResponsavel"));
            dto.setEmailResponsavel(ctx.formParam("emailResponsavel"));
            dto.setCodigoInep(ctx.formParam("codigoInep"));
            dto.setSituacaoFuncionamento(ctx.formParam("situacaoFuncionamento"));
            if (ctx.formParam("dataInicioAnoLetivo") != null && !ctx.formParam("dataInicioAnoLetivo").isBlank()) dto.setDataInicioAnoLetivo(java.time.LocalDate.parse(ctx.formParam("dataInicioAnoLetivo")));
            if (ctx.formParam("dataTerminoAnoLetivo") != null && !ctx.formParam("dataTerminoAnoLetivo").isBlank()) dto.setDataTerminoAnoLetivo(java.time.LocalDate.parse(ctx.formParam("dataTerminoAnoLetivo")));
            dto.setLatitude(ctx.formParam("latitude"));
            dto.setLongitude(ctx.formParam("longitude"));
            dto.setZona(ctx.formParam("zona"));
            dto.setLocalizacaoDiferenciada(ctx.formParam("localizacaoDiferenciada"));
            dto.setDependenciaAdministrativa(ctx.formParam("dependenciaAdministrativa"));
            dto.setRegulamentacaoNumero(ctx.formParam("regulamentacaoNumero"));
            if (ctx.formParam("regulamentacaoData") != null && !ctx.formParam("regulamentacaoData").isBlank()) dto.setRegulamentacaoData(java.time.LocalDate.parse(ctx.formParam("regulamentacaoData")));
            dto.setInfraAgua(ctx.formParam("infraAgua"));
            dto.setInfraEnergia(ctx.formParam("infraEnergia"));
            dto.setInfraEsgoto(ctx.formParam("infraEsgoto"));
            dto.setInfraLixo(ctx.formParam("infraLixo"));
            if (ctx.formParam("qtdComputadores") != null && !ctx.formParam("qtdComputadores").isBlank()) dto.setQtdComputadores(Integer.parseInt(ctx.formParam("qtdComputadores")));
            dto.setTemInternet("on".equals(ctx.formParam("temInternet")) || "true".equals(ctx.formParam("temInternet")));
            dto.setTipoBandaLarga(ctx.formParam("tipoBandaLarga"));
            dto.setLinguaMinistrada(ctx.formParam("linguaMinistrada"));


            CriarEscolaResponseDTO resultado = escolaService.cadastrarEscola(dto, currentUser);

            // Flash de sessão só pra essa próxima requisição — a senha em texto puro
            // nunca é salva em lugar nenhum, é mostrada uma única vez pro Super Admin.
            ctx.sessionAttribute("gestorEmailGerado", resultado.getEmailGestor());
            String cpfGestorBruto = resultado.getCpfGestor();
            String cpfGestorFormatado = (cpfGestorBruto != null && cpfGestorBruto.length() == 11)
                    ? cpfGestorBruto.substring(0, 3) + "." + cpfGestorBruto.substring(3, 6) + "." + cpfGestorBruto.substring(6, 9) + "-" + cpfGestorBruto.substring(9, 11)
                    : cpfGestorBruto;
            ctx.sessionAttribute("gestorCpfGerado", cpfGestorFormatado);
            ctx.sessionAttribute("gestorSenhaGerada", resultado.getSenhaGeradaGestor());

            ctx.status(201);
            ctx.redirect("/dashboard/escolas");

        } catch (Exception e) {
            ctx.status(400);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH ou POST /escolas/{id} - Atualiza uma escola
     */
    /**
     * PATCH ou POST /escolas/{id} - Atualiza uma escola
     */
    public void atualizarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            AtualizarEscolaDTO dto;

            // Detecta se o envio veio de um formulário HTML convencional
            if (ctx.formParamMap() != null && !ctx.formParamMap().isEmpty()) {
                dto = new AtualizarEscolaDTO();

                // 💡 CORREÇÃO CRÍTICA: Usamos um método auxiliar 'obterCampoForm'
                // para garantir que campos vazios virem 'null' e não quebrem a validação do Service
                dto.setNome(obterCampoForm(ctx, "nome"));
                dto.setCnpj(obterCampoForm(ctx, "cnpj"));
                dto.setEmailInstitucional(obterCampoForm(ctx, "emailInstitucional"));
                dto.setTelefone(obterCampoForm(ctx, "telefone"));
                dto.setCidade(obterCampoForm(ctx, "cidade"));
                dto.setEstado(obterCampoForm(ctx, "estado"));
                dto.setNomeResponsavel(obterCampoForm(ctx, "nomeResponsavel"));

                // Campos opcionais (que podem não estar no seu HTML atual)
                dto.setEndereco(obterCampoForm(ctx, "endereco"));
                dto.setNumero(obterCampoForm(ctx, "numero"));
                dto.setComplemento(obterCampoForm(ctx, "complemento"));
                dto.setBairro(obterCampoForm(ctx, "bairro"));
                dto.setCep(obterCampoForm(ctx, "cep"));
                dto.setTelefoneResponsavel(obterCampoForm(ctx, "telefoneResponsavel"));
                dto.setEmailResponsavel(obterCampoForm(ctx, "emailResponsavel"));
                dto.setStatus(obterCampoForm(ctx, "status"));
                dto.setCodigoInep(obterCampoForm(ctx, "codigoInep"));
                dto.setSituacaoFuncionamento(obterCampoForm(ctx, "situacaoFuncionamento"));
                if (obterCampoForm(ctx, "dataInicioAnoLetivo") != null) dto.setDataInicioAnoLetivo(java.time.LocalDate.parse(obterCampoForm(ctx, "dataInicioAnoLetivo")));
                if (obterCampoForm(ctx, "dataTerminoAnoLetivo") != null) dto.setDataTerminoAnoLetivo(java.time.LocalDate.parse(obterCampoForm(ctx, "dataTerminoAnoLetivo")));
                dto.setLatitude(obterCampoForm(ctx, "latitude"));
                dto.setLongitude(obterCampoForm(ctx, "longitude"));
                dto.setZona(obterCampoForm(ctx, "zona"));
                dto.setLocalizacaoDiferenciada(obterCampoForm(ctx, "localizacaoDiferenciada"));
                dto.setDependenciaAdministrativa(obterCampoForm(ctx, "dependenciaAdministrativa"));
                dto.setRegulamentacaoNumero(obterCampoForm(ctx, "regulamentacaoNumero"));
                if (obterCampoForm(ctx, "regulamentacaoData") != null) dto.setRegulamentacaoData(java.time.LocalDate.parse(obterCampoForm(ctx, "regulamentacaoData")));
                dto.setInfraAgua(obterCampoForm(ctx, "infraAgua"));
                dto.setInfraEnergia(obterCampoForm(ctx, "infraEnergia"));
                dto.setInfraEsgoto(obterCampoForm(ctx, "infraEsgoto"));
                dto.setInfraLixo(obterCampoForm(ctx, "infraLixo"));
                if (obterCampoForm(ctx, "qtdComputadores") != null) dto.setQtdComputadores(Integer.parseInt(obterCampoForm(ctx, "qtdComputadores")));
                dto.setTemInternet("on".equals(ctx.formParam("temInternet")) || "true".equals(ctx.formParam("temInternet")));
                dto.setTipoBandaLarga(obterCampoForm(ctx, "tipoBandaLarga"));
                dto.setLinguaMinistrada(obterCampoForm(ctx, "linguaMinistrada"));

            } else {
                // Se for uma requisição de API com JSON puro
                dto = ctx.bodyAsClass(AtualizarEscolaDTO.class);
            }

            Escola escola = escolaService.atualizarEscola(escolaId, dto, currentUser);

            // Redirecionamento automático se veio da tela do painel
            String referer = ctx.header("Referer");
            if (referer != null && referer.contains("/dashboard/")) {
                ctx.redirect("/dashboard/escolas");
                return;
            }

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
     * 💡 Método Auxiliar: Retorna null se o campo do formulário for nulo,
     * vazio ou contiver apenas espaços. Evita erros no Service.
     */
    private String obterCampoForm(Context ctx, String nomeCampo) {
        String valor = ctx.formParam(nomeCampo);
        if (valor == null || valor.isBlank() || "null".equalsIgnoreCase(valor.trim())) {
            return null;
        }
        return valor.trim();
    }

    /**
     * GET /escolas/{id} - Busca uma escola por ID
     */
    public void obtenerEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID schoolId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.buscarEscolaPorId(schoolId, currentUser);

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
    private static int paginationParam(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return new java.math.BigInteger(value.trim()).max(java.math.BigInteger.valueOf(Integer.MIN_VALUE))
                    .min(java.math.BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void listarEscolas(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            EscolaStatus status = EscolaStatus.fromFilter(ctx.queryParam("status"));
            String search = ctx.queryParam("search");
            PageResponse<EscolaResumoDTO> pagina = escolaService.listarPaginadas(search, status,
                    paginationParam(ctx.queryParam("page"), 1),
                    paginationParam(ctx.queryParam("size"), 20), currentUser);
            ctx.status(HttpStatus.OK);
            ctx.json(pagina);

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
     * PATCH ou POST /escolas/{id}/ativar - Ativa uma escola
     */
    public void ativarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.ativarEscola(escolaId, currentUser);

            // 💡 REDIRECIONAMENTO AUTOMÁTICO: Evita a tela de JSON puro após ativar
            String referer = ctx.header("Referer");
            if (referer != null && referer.contains("/dashboard/")) {
                ctx.redirect("/dashboard/escolas");
                return;
            }

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
     * PATCH ou POST /escolas/{id}/inativar - Inativa uma escola
     */
    public void inativarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.inativarEscola(escolaId, currentUser);

            // 💡 REDIRECIONAMENTO AUTOMÁTICO: Evita a tela de JSON puro após inativar
            String referer = ctx.header("Referer");
            if (referer != null && referer.contains("/dashboard/")) {
                ctx.redirect("/dashboard/escolas");
                return;
            }

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

    /**
     * DELETE /escolas/{id}/excluir - Exclui definitivamente a escola (hard delete).
     */
    public void excluirEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            escolaService.excluirEscola(escolaId, currentUser);

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Escola excluída definitivamente com sucesso."));
        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na exclusão de escola: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Erro na exclusão de escola: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de escola inválido."));
            logger.warn("ID de escola inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao excluir escola."));
        }
    }

    /**
     * POST /dashboard/escolas/{id}/deletar - Exclui uma escola (soft delete)
     * Usa POST pois formulários HTML não suportam DELETE nativamente.
     */
    public void deletarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            escolaService.deletarEscola(escolaId, currentUser);

            String referer = ctx.header("Referer");
            if (referer != null && referer.contains("/dashboard/")) {
                ctx.redirect("/dashboard/escolas");
                return;
            }

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Escola excluída com sucesso."));

        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao excluir escola: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Erro ao excluir escola: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de escola inválido."));
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir escola: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Erro interno ao excluir escola."));
        }
    }

    public void exibirPaginaListagem(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                ctx.redirect("/login");
                return;
            }

            EscolaStatus status = EscolaStatus.fromFilter(ctx.queryParam("status"));
            String search = ctx.queryParam("search");
            PageResponse<EscolaResumoDTO> pagina = escolaService.listarPaginadas(search, status,
                    paginationParam(ctx.queryParam("page"), 1),
                    paginationParam(ctx.queryParam("size"), 20), currentUser);
            List<EscolaResumoDTO> escolas = pagina.items();

            Map<String, Object> model = new java.util.HashMap<>();
            model.put("escolas", escolas);
            model.put("filtroStatus", status == null ? "" : status.name());
            model.put("content", "dashboard/escolas/lista");
            model.put("search", search == null ? "" : search.trim());
            model.put("page", pagina.page());
            model.put("size", pagina.size());
            model.put("total", pagina.totalItems());
            model.put("totalPages", pagina.totalPages());
            model.put("hasPrevious", pagina.hasPrevious());
            model.put("hasNext", pagina.hasNext());

            String gestorEmail = ctx.sessionAttribute("gestorEmailGerado");
            String gestorCpf = ctx.sessionAttribute("gestorCpfGerado");
            String gestorSenha = ctx.sessionAttribute("gestorSenhaGerada");
            if (gestorEmail != null && gestorSenha != null) {
                model.put("gestorEmailGerado", gestorEmail);
                model.put("gestorCpfGerado", gestorCpf);
                model.put("gestorSenhaGerada", gestorSenha);
                // flash: mostra só uma vez, some depois desse render
                ctx.sessionAttribute("gestorEmailGerado", null);
                ctx.sessionAttribute("gestorCpfGerado", null);
                ctx.sessionAttribute("gestorSenhaGerada", null);
            }

            ctx.render("dashboard/escolas/lista.html", model);

        } catch (Exception e) {
            logger.error("Erro ao carregar página de listagem", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro ao carregar página.");
        }
    }

    /**
     * GET /dashboard/escolas/visualizar/{id} - Renderiza a página de detalhes da escola
     */
    public void exibirPaginaVisualizar(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                ctx.redirect("/login");
                return;
            }

            UUID escolaId = UUID.fromString(ctx.pathParam("id"));
            Escola escola = escolaService.buscarEscolaPorId(escolaId, currentUser);

            Map<String, Object> model = Map.of("escola", escola);
            ctx.render("dashboard/escolas/visualizar.html", model);

        } catch (Exception e) {
            logger.error("Erro ao carregar página de visualização", e);
            ctx.redirect("/dashboard/escolas");
        }
    }
}
