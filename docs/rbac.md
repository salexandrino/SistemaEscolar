# RBAC e isolamento por tenant

O `AuthMiddleware` valida a sessao JWT e disponibiliza o usuario autenticado em
`AuthUserContext`. As rotas protegidas usam `AuthorizationMiddleware`, que
consulta exclusivamente `Perfil.hasPermission(Permissao)`.

O middleware retorna `AuthenticationException` para usuario ausente (HTTP 401)
e `AuthorizationException` para permissao insuficiente (HTTP 403). Operacoes
globais recebem tambem o perfil `SUPER_ADMIN`, pois exigem permissao e escopo
global; escolas e usuarios globais, auditoria e o dashboard administrativo
seguem esse modelo.

Recursos de uma escola usam permissoes do modulo (alunos, matriculas, turmas,
notas e financeiro). O `TenantAccessGuard` e a fonte unica do tenant da
requisicao: services e controllers consultam o tenant do usuario autenticado e
repositorios filtram por `tenant_id`. Quando o tenant do recurso nao coincide,
o guard devolve 404 para nao revelar a existencia do recurso de outra escola.

A autorizacao no backend e obrigatoria. Os templates Thymeleaf apenas recebem
flags calculadas no servidor para ocultar acoes que o perfil nao pode executar;
isso nao substitui os middlewares das rotas.
