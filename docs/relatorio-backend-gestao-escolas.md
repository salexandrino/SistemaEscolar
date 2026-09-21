# Relatório do Backend de Gestão de Escolas

**Projeto:** Kutuar Educação  
**Data da análise:** 2026-09-21  
**Escopo:** backend Java/Javalin responsável pelo cadastro, consulta, atualização, ativação, inativação e exclusão de escolas, incluindo a criação do gestor inicial, isolamento multi-tenant e dashboard administrativo.

## 1. Resumo executivo

O backend usa uma arquitetura manual em camadas:

```text
Requisição HTTP
  -> AuthMiddleware/JWT
  -> rota Javalin em KutuarApp
  -> EscolaController ou controller de dashboard
  -> EscolaService
  -> EscolaRepository/UsuarioRepository/DashboardRepository
  -> PostgreSQL via JDBC e PreparedStatement
```

A gestão global de escolas é uma função de administração da plataforma e exige o perfil `SUPER_ADMIN`. Cada escola possui um `id` e um `tenantId`; o `tenantId` da escola é usado pelos módulos acadêmico e financeiro para separar alunos, turmas, usuários e movimentações.

O cadastro de uma escola também cria automaticamente o primeiro usuário com perfil `GESTOR`, gera uma senha temporária, grava somente o hash da senha e dispara observadores de auditoria e envio de e-mail.

## 2. Componentes envolvidos

### Inicialização e composição

- `src/main/java/br/com/kutuar/KutuarApp.java`
  - Inicializa banco e Flyway antes de subir o servidor.
  - Instancia repositories, services e controllers manualmente.
  - Registra `EscolaService` com `AuditLogEscolaObserver` e `EmailGestorObserver`.
  - Registra o `AuthMiddleware` globalmente.
  - Publica as rotas HTML, REST e API.
- `src/main/java/br/com/kutuar/config/DatabaseConfig.java`
  - Fornece a conexão usada pela camada de persistência.
- `src/main/java/br/com/kutuar/config/FlywayConfig.java`
  - Executa as migrations no startup.

### Camada HTTP

- `seguranca/controllers/EscolaController.java`
  - Converte parâmetros de formulário ou JSON em DTOs.
  - Lê o usuário autenticado do `AuthUserContext`.
  - Delega regras ao service.
  - Converte entidades para mapas JSON e decide entre JSON e redirecionamento do dashboard.
  - Trata erros de autenticação, autorização, validação, conflito, inexistência e erro interno.
- `seguranca/controllers/EscolaDashboardController.java`
  - Renderiza páginas Thymeleaf de listagem, visualização, edição e criação.
- `administrativo/controllers/DashboardController.java`
  - Renderiza o dashboard master e páginas administrativas de escolas.
- `administrativo/controllers/SuperAdminDashboardApiController.java`
  - Expõe os dados agregados do dashboard administrativo.

### Regras e persistência

- `seguranca/services/EscolaService.java`
  - É o dono das regras de autorização, validação, ciclo de vida e criação do gestor.
- `seguranca/repositories/EscolaRepository.java`
  - Executa SQL JDBC da tabela `escola`.
  - Faz o mapeamento completo `ResultSet -> Escola`.
- `seguranca/repositories/UsuarioRepository.java`
  - Cria, busca, inativa e exclui usuários associados ao tenant.
- `administrativo/services/DashboardService.java` e `DashboardRepository.java`
  - Calculam contagens, crescimento, recentes, alertas, acessos e atividades.

## 3. Autenticação, autorização e multi-tenant

1. O `AuthMiddleware` executa no início de toda requisição.
2. Ele lê o cookie JWT, valida o token com `JwtService` e coloca o `AuthUser` em `AuthUserContext` e em `ctx.attribute("currentUser")`.
3. Rotas públicas continuam acessíveis sem token; cada rota protegida ou service decide quando rejeitar usuário ausente.
4. O método privado `verificarPermissaoMaster` do `EscolaService` rejeita qualquer perfil diferente de `SUPER_ADMIN`, inclusive `GESTOR`, `SECRETARIA`, `PROFESSOR` e `FINANCEIRO`.
5. O usuário autenticado carrega `userId`, `tenantId`, `perfil` e `cpf`.
6. Ao criar a escola, o repository gera um UUID para a escola e outro UUID para `tenant_id`. Depois o gestor inicial recebe `tenantId` e `escolaId` iguais ao `id` da escola.
7. Os módulos acadêmico e financeiro recebem o tenant do usuário autenticado e fazem suas consultas com esse valor. O CRUD administrativo de escolas é global e não usa o tenant do administrador para restringir a lista.

### Rotas administrativas protegidas por perfil

Além da verificação no service, `KutuarApp` protege áreas relacionadas:

- `/api/admin/dashboard` e subrotas: apenas `SUPER_ADMIN`.
- `/api/academico/*`: dividido entre rotas docentes e administrativas.
- `/api/financeiro/*`: `SUPER_ADMIN`, `GESTOR` e `FINANCEIRO`.
- Portal de escola: permissões por perfil para anos letivos, séries, professores, turmas, alunos e boletins.

## 4. Endpoints de escolas

### API REST principal

| Método | Rota | Comportamento |
|---|---|---|
| `POST` | `/escolas` | Cria escola e gestor inicial; para formulário, redireciona ao dashboard. |
| `PATCH` ou `POST` | `/escolas/{id}` | Atualização parcial via formulário ou JSON. |
| `GET` | `/escolas` | Lista todas as escolas; suporta `?status=ativa` ou `?status=inativa`. |
| `GET` | `/escolas/ativas` | Lista somente escolas com status `ATIVA`. |
| `GET` | `/escolas/inativas` | Lista somente escolas com status `INATIVA`. |
| `GET` | `/escolas/{id}` | Busca dados completos por UUID. |
| `GET` | `/escolas/cnpj/{cnpj}` | Busca uma escola por CNPJ. |
| `PATCH` ou `POST` | `/escolas/{id}/ativar` | Troca status para `ATIVA`. |
| `PATCH` ou `POST` | `/escolas/{id}/inativar` | Troca status para `INATIVA`. |
| `DELETE` | `/escolas/{id}/excluir` | Exclusão física, com pré-condições fortes. |
| `POST` | `/dashboard/escolas/{id}/deletar` | Exclusão pelo fluxo HTML do dashboard. |

Também existem aliases de consulta em `/api/escolas` e `/api/escolas/{id}`.

### Respostas

- Listagens retornam `{ total, escolas }`.
- Listagens usam representação resumida: `id`, `tenantId`, `nome`, `cnpj`, `status`, `criadoEm` e `atualizadoEm`.
- Consultas individuais retornam todos os campos mapeados da escola.
- Atualizações e alterações de status retornam mensagem e escola resumida, exceto quando a origem é o dashboard, caso em que redirecionam para `/dashboard/escolas`.
- Erros de API normalmente retornam `{ "message": "..." }`; o cadastro usa `{ "error": "..." }` no catch genérico.

## 5. Fluxo de cadastro de escola

1. `EscolaController.criarEscola` exige um usuário no contexto.
2. Lê campos `formParam`, incluindo endereço, responsável, dados INEP, calendário letivo, localização, infraestrutura e idioma.
3. Converte datas para `LocalDate`, quantidade de computadores para `Integer` e checkbox de internet para `boolean`.
4. Chama `EscolaService.cadastrarEscola(dto, currentUser)`.
5. O service verifica `SUPER_ADMIN` e valida nome, CNPJ, e-mail, endereço, UF, CEP, responsável, INEP, coordenadas, datas e campos enumerados.
6. Valida o CPF do futuro gestor e verifica conflito de CNPJ e de e-mail do responsável.
7. Cria a entidade `Escola` com status inicial `ATIVA`.
8. `EscolaRepository.save` gera `id`, `tenantId`, `criadoEm` e `atualizadoEm` e executa `INSERT` parametrizado.
9. O service gera senha temporária de 12 caracteres com maiúscula, minúscula, número e caractere especial.
10. Cria o usuário gestor com perfil `GESTOR`, `ativo=true`, `bloqueado=false`, tenant da escola e senha armazenada como hash por `PasswordService`.
11. Executa `UsuarioRepository.save` para persistir o gestor.
12. Notifica os observers:
    - `AuditLogEscolaObserver`: registra a criação.
    - `EmailGestorObserver`: envia as credenciais pelo `EmailService`.
13. O controller guarda e-mail, CPF formatado e senha em atributos de sessão para exibição única ao Super Admin e redireciona para a listagem.

### Dados sensíveis do cadastro

A senha em texto puro não é salva no banco, mas é mantida temporariamente no objeto de resposta e em atributo de sessão para ser exibida ao administrador. O gestor deve recebê-la no primeiro acesso e trocá-la conforme o fluxo de autenticação.

## 6. Atualização e validações

O update é parcial: campos nulos não são alterados; campos textuais enviados pelo formulário são normalizados para `null` quando vazios. O `EscolaRepository.update` preserva `id`, `tenantId` e `criadoEm`, atualizando `atualizadoEm`.

Validações principais:

- nome entre 3 e 150 caracteres;
- CNPJ obrigatório no cadastro, válido e único;
- e-mail institucional válido;
- endereço, bairro, cidade, UF, CEP e responsável válidos;
- status apenas `ATIVA` ou `INATIVA`;
- código INEP com 8 caracteres quando informado;
- latitude e longitude devem existir juntas e respeitar os limites geográficos;
- término do ano letivo não pode anteceder o início;
- situação: `EM_ATIVIDADE`, `PARALISADA` ou `EXTINTA`;
- dependência: `FEDERAL`, `ESTADUAL`, `MUNICIPAL` ou `PRIVADA`;
- zona: `URBANA` ou `RURAL`;
- língua: `PORTUGUESA`, `INDIGENA` ou `BILINGUE`;
- limites de tamanho para número e complemento.

O campo `temInternet` é atribuído também durante atualização; como é primitivo, ausência do campo no payload pode resultar em `false`.

## 7. Estados e exclusões

### Ativação e inativação

- `ativarEscola` busca a escola, rejeita se já estiver `ATIVA`, altera o status e persiste.
- `inativarEscola` busca a escola, rejeita se já estiver `INATIVA`, altera o status e persiste.
- Ambas as operações são mudanças de estado, não removem a linha nem os dados vinculados.

### Exclusão pelo dashboard

`deletarEscola` é descrita como soft delete, mas a implementação atual chama `escolaRepository.delete(id)`, que executa `DELETE FROM escola`. Antes disso, inativa os usuários encontrados pelo `escolaId`. Portanto, o comportamento efetivo atual é exclusão física da escola, não apenas alteração para `EXCLUIDA`.

### Exclusão definitiva pela API

`excluirEscola` exige:

1. usuário `SUPER_ADMIN`;
2. escola existente;
3. status previamente `INATIVA`;
4. ausência de registros em `aluno`, `turma` e `mensalidade` com aquele tenant.

Se a consulta de dados vinculados falhar, o repository assume que existem vínculos e bloqueia a exclusão. Quando liberada, a operação remove primeiro os usuários do tenant e depois a escola, respeitando a FK `usuario.tenant_id -> escola.id`.

## 8. Modelo de dados da escola

A tabela `escola` é criada em `src/main/resources/db/migration/V1__schema_inicial.sql` e complementada por `db/migration/V5__ensure_escola_columns.sql`.

Campos persistidos e expostos pelo model:

- identidade: `id`, `tenant_id`;
- identificação: `nome`, `cnpj`, `codigo_inep`, `status`;
- contato institucional: e-mail e telefone;
- endereço: endereço, número, complemento, bairro, cidade, estado e CEP;
- responsável: nome, CPF no cadastro, telefone e e-mail;
- funcionamento: situação, início e término do ano letivo;
- localização: latitude, longitude, zona e localização diferenciada;
- administração: dependência administrativa, regulamentação e data;
- infraestrutura: água, energia, esgoto, lixo, quantidade de computadores, internet e banda larga;
- educação: língua ministrada;
- auditoria: `criado_em` e `atualizado_em`.

O CNPJ é verificado pela aplicação antes de criar ou alterar. A migration V5 informa que não adiciona automaticamente uma constraint `UNIQUE`, portanto a unicidade depende da validação da aplicação e pode sofrer condição de corrida em requisições simultâneas.

## 9. Dashboard administrativo

O dashboard Super Admin consulta diretamente o banco por `DashboardRepository` e calcula:

- total de escolas;
- escolas ativas e inativas;
- últimas cinco escolas;
- crescimento mensal de escolas nos últimos seis meses;
- total e distribuição de usuários por perfil;
- usuários pendentes, bloqueados, sem acesso recente e com tentativas suspeitas;
- últimos acessos e atividades recentes.

Os alertas levam para filtros como `/dashboard/escolas?status=inativa` e `/dashboard/usuarios?...`. O dashboard não é apenas uma camada visual: suas contagens e listas são produzidas por SQL próprio, separado do `EscolaRepository`.

## 10. Tratamento de erros e observabilidade

- `AuthenticationException`: normalmente redireciona para login nas exceções globais; handlers locais podem responder status HTTP.
- `AuthorizationException`: `403`.
- `ValidationException`: `400`.
- `ConflictException`: `409`.
- `NotFoundException`: `404`.
- falhas inesperadas: `500`.
- repositories usam `PreparedStatement`, try-with-resources e logs SLF4J.
- operações de escola geram logs de criação, atualização, ativação, inativação e exclusão.
- o cadastro usa observers para desacoplar auditoria e e-mail do fluxo principal.

## 11. Testes encontrados

- `src/test/java/br/com/kutuar/seguranca/models/EscolaTest.java`
  - verifica preservação de nome e CNPJ;
  - verifica status padrão `ATIVA` em uma simulação do comportamento do repository.
- `src/test/java/br/com/kutuar/administrativo/DashboardPostgresTest.java`
  - teste opt-in por variável `KUTUAR_DASHBOARD_DB_TEST=true`;
  - testa contagens, crescimento mensal, limites de recentes, dados ausentes e renderização Thymeleaf.
- Os relatórios Surefire existentes mostram testes de serviços acadêmicos, autenticação, usuários e dashboard.

Não foi encontrada, nesta análise, uma suíte dedicada que exercite de ponta a ponta `EscolaController -> EscolaService -> EscolaRepository`, incluindo autorização por perfil, criação atômica de escola+gestor, conflitos, exclusões e isolamento entre tenants.

## 12. Pontos de atenção técnicos

1. **Cadastro sem transação explícita:** escola e gestor são gravados em operações separadas. Se a segunda gravação falhar, pode existir escola sem gestor inicial.
2. **Exclusão do dashboard contradiz o comentário:** `deletarEscola` fala em soft delete/status `EXCLUIDA`, mas chama `DELETE` físico e não executa `setStatus("EXCLUIDA")`.
3. **Unicidade de CNPJ somente na aplicação:** sem constraint única no banco, duas requisições concorrentes podem passar pela consulta e gravar o mesmo CNPJ.
4. **Senha temporária em sessão:** é uma decisão funcional necessária para entrega única, mas aumenta o impacto de exposição da sessão administrativa; a sessão deve ser protegida e expirar adequadamente.
5. **Resposta de cadastro inconsistente:** outros handlers usam `message`, enquanto o catch genérico de criação usa `error` e transforma qualquer exceção em `400`, inclusive falhas que poderiam ser `409` ou `500`.
6. **Escopo das listagens do Super Admin:** as consultas retornam todas as escolas, o que é esperado para administração global, mas exige proteção rigorosa da autorização.
7. **Cobertura de integração insuficiente para o risco:** as regras críticas estão principalmente em código de produção e testes de model/dashboard, sem uma matriz completa de casos do CRUD.

## 13. Conclusão

O backend de gestão de escolas funciona como o cadastro mestre de tenants da plataforma. Ele controla a identidade institucional, o status operacional, o gestor inicial e a entrada dos dados nos módulos acadêmico e financeiro. O desenho atual tem separação clara entre controller, service e repository, usa SQL parametrizado e aplica autorização de Super Admin no service.

As prioridades técnicas para endurecimento são: envolver o cadastro e a criação do gestor em uma transação, corrigir a divergência entre soft delete e `DELETE` físico, adicionar unicidade de CNPJ no banco e ampliar testes de integração das operações administrativas e do isolamento multi-tenant.