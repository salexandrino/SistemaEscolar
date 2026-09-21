# Relatório de revisão — feat/superadmin-dashboard-backend

Data: 20/09/2026. Commit analisado: `8d8f4c5`.

## Parecer

A branch entrega uma API de dashboard para o Super Admin, com indicadores gerais, alertas administrativos, atividades recentes e últimos acessos. A estrutura separa controller, serviço, repositório e DTOs, protege as rotas por perfil e evita recalcular todo o dashboard ao consultar apenas alertas.

**A implementação compila e os sete testes unitários do dashboard passam, mas há dois problemas funcionais a corrigir antes da homologação:** o JSON apresenta estados incorretos dos usuários recentes e os links dos novos alertas não aplicam os filtros esperados nas telas de destino. As consultas novas e a autorização dos endpoints ainda precisam de validação de integração.

Este relatório corresponde à branch de **backend**. As mudanças herdadas da branch de UI são identificadas separadamente, sem atribuí-las ao commit exclusivo de backend.

## Escopo e método

| Comparação | Resultado |
| --- | --- |
| Backend `8d8f4c5` contra UI `3ad7b4d` | **1 commit exclusivo; 10 arquivos alterados; 463 adições e 1 remoção.** |
| Backend contra `origin/main` disponível localmente (`4f05d72`), usando ancestral comum `156a4e8` | **8 commits exclusivos; 16 arquivos alterados; 923 adições e 264 remoções.** Inclui mudanças herdadas da UI. |

O único commit exclusivo tem a mensagem `fix(dashboard): otimiza buscarAlertasSistema para nao reprocessar o dashboard completo`, mas seu conteúdo é maior: inclui toda a nova API, os DTOs, consultas, composição da resposta e testes adicionais.

Foram examinados todos os dez arquivos do incremento de backend, as mudanças herdadas no diff contra a base remota e os pontos de integração necessários: autenticação, autorização, modelo de usuário, registro de login, esquema SQL e controllers das telas de destino.

A revisão foi feita em uma cópia isolada do commit `8d8f4c5`. Durante o trabalho, o workspace principal mudou de branch e passou a apresentar um merge em andamento, com conflito no `pom.xml`. Esse estado não foi utilizado para compilar a branch revisada nem foi alterado por esta revisão. Não houve `fetch`; as referências remotas são as disponíveis localmente.

Os caminhos e números de linha citados referem-se ao commit analisado.

## O que foi implementado

### API e controle de acesso

As quatro rotas são GET e retornam JSON:

| Endpoint | Conteúdo | Consultas por chamada, pela leitura do código |
| --- | --- | --- |
| `/api/admin/dashboard` | Dados básicos, gráficos, escolas/usuários recentes, alertas, atividades e acessos | 12 |
| `/api/admin/dashboard/alertas` | Alertas com quantidade positiva | 5 |
| `/api/admin/dashboard/atividades` | Até 15 atividades | 1 |
| `/api/admin/dashboard/ultimos-acessos` | Até 10 usuários com último login registrado | 1 |

`KutuarApp.java:663–668` registra proteção para o caminho exato e seus descendentes com `RoleBasedMiddleware(Perfil.SUPER_ADMIN)`. O middleware global de autenticação preenche o contexto do usuário. O middleware de perfil rejeita ausência de usuário e perfis diferentes de Super Admin; o tratamento de autorização existente produz HTTP 403 com JSON nas rotas de API.

As consultas são globais, sem filtro por escola/tenant, coerentes com esse acesso administrativo. A proteção foi examinada estaticamente; não foi realizado teste HTTP autenticado para demonstrar o bloqueio de cada perfil.

### Alertas

Foram implementados cinco tipos:

- Escolas inativas.
- Usuários pendentes, definidos pelo campo `ativo` não verdadeiro.
- Usuários bloqueados.
- Usuários ativos sem último login ou com login anterior a 30 dias.
- Usuários com três ou mais tentativas de login acumuladas.

Alertas com quantidade menor ou igual a zero são omitidos. Quantidades de 1 a 10 recebem `ATENCAO`; acima de 10 recebem `CRITICO`, independentemente do tipo.

O endpoint específico de alertas consulta somente cinco contagens. Na resposta completa, as contagens de escolas inativas e usuários pendentes são reaproveitadas do dashboard básico, evitando repeti-las.

### Atividades e últimos acessos

`findUltimosAcessos` usa `LEFT JOIN` com escola, permite usuários sem escola associada, exclui último login nulo e retorna até dez resultados por data decrescente e ID.

`findAtividadesRecentes` combina cadastros de escolas, cadastros de usuários e último login de cada usuário com `UNION ALL`, ordenando e limitando o conjunto a quinze itens.

As colunas `bloqueado`, `tentativas_login` e `ultimo_login` já existem em `V1__schema_inicial.sql`. O fluxo de autenticação existente atualiza `ultimo_login` após login bem-sucedido e persiste tentativas malsucedidas. O incremento de backend não adiciona migrações.

## Achados funcionais

### 1. Média — JSON de usuários recentes informa status que não foi carregado

**Locais:** `src/main/java/br/com/kutuar/administrativo/services/DashboardService.java:153–154`, `dto/SuperAdminDashboardResponseDTO.java:5` e `repositories/DashboardRepository.java:108–140`.

A nova resposta herda `usuariosRecentes` do DTO básico, usando objetos do modelo `Usuario`. Porém, `findUltimosUsuarios()` seleciona e preenche apenas ID, nome, perfil e data de criação. O campo `ativo` não é lido.

Ao serializar esses objetos, os getters do modelo também expõem `ativo`, `aprovado`, `pendente` e `status`. Como o booleano interno permanece no valor padrão, o JSON informa `ativo=false` e `status="PENDENTE"` inclusive para usuários ativos no banco. Campos de bloqueio e outras propriedades não carregadas também ficam com valores padrão ou nulos.

**Reprodução:** foi executado um teste diagnóstico na cópia isolada usando o repositório real com JDBC simulado e serialização Jackson. O resultado simulado disponibilizava `ativo=true`, mas o repositório não leu esse campo; o JSON resultante continha `ativo=false` e `status="PENDENTE"`. O teste confirmou o comportamento, sem conexão com banco.

**Impacto:** consumidores da nova API podem exibir situação incorreta ou tomar decisões com base em campos que aparentam estar preenchidos corretamente.

**Correção sugerida:** criar um DTO específico para usuário recente, expondo apenas dados efetivamente consultados. Caso o contrato necessite do status, selecionar e mapear os campos correspondentes. Evitar serializar diretamente entidades parcialmente carregadas.

**Origem:** o mapeamento parcial foi herdado da UI, mas sua exposição como resposta JSON é introduzida neste incremento de backend. Não foi constatado vazamento de senhas: os campos sensíveis não são carregados nessa consulta.

### 2. Média — links dos novos alertas abrem listagens sem a filtragem prometida

**Local:** `src/main/java/br/com/kutuar/administrativo/services/DashboardService.java:86–99` e `115–128`.

Os alertas retornam os seguintes links:

| Link gerado | Comportamento do destino |
| --- | --- |
| `/dashboard/usuarios?status=bloqueado` | O controller não reconhece `bloqueado`; cai no filtro padrão de usuários ativos. |
| `/dashboard/usuarios?acesso=sem-acesso-recente` | O parâmetro `acesso` não é lido; a listagem padrão de ativos é apresentada. |
| `/dashboard/usuarios?seguranca=tentativas-login` | O parâmetro `seguranca` não é lido; a listagem padrão de ativos é apresentada. |

**Evidência:** `DashboardController.java:99–118` trata apenas `status=pendente`, `inativos` e `todos`, com fallback para usuários ativos. Os outros dois parâmetros não são consultados nesse fluxo.

**Impacto:** ao seguir o alerta, o administrador recebe registros diferentes do grupo contado, dificultando encontrar os usuários que exigem ação.

**Correção sugerida:** implementar os filtros nos destinos com os mesmos critérios das consultas de alerta ou fornecer links para telas que realmente suportem a ação. Validar quantidade e conteúdo da lista após seguir cada link.

O link de escolas inativas também depende de um filtro que o controller de escolas não aplica. Essa deficiência já existia na UI; o backend agora a reutiliza no contrato de alertas.

## Limitações e decisões que precisam ficar explícitas

- **As novas informações ainda não aparecem no dashboard HTML.** `DashboardController.dashboard()` continua chamando `buscarDashboard()`, e não há consumidor das novas rotas em `src/main/resources`. Isso não impede a entrega de uma API isolada, mas a branch não conclui a integração visual dos novos alertas, acessos e atividades.
- **Atividades não são um histórico de auditoria.** Um novo login sobrescreve `usuario.ultimo_login`; o login anterior deixa de aparecer nas consultas. Exclusões de usuários/escolas também removem os eventos derivados desses registros. Caso seja necessário histórico permanente, será preciso armazenar eventos separados.
- **Novos usuários sem login entram imediatamente no alerta de 30 dias.** A consulta usa `ultimo_login IS NULL` sem considerar `criado_em`. A mensagem pode ser entendida como inatividade prolongada; alinhar a regra de negócio ou distinguir “nunca acessou” de “sem acesso há mais de 30 dias”.
- **Tentativas suspeitas contam usuários, não eventos em uma janela temporal.** O filtro é `tentativas_login >= 3`, e o login bem-sucedido zera o contador no fluxo existente. O tipo e a mensagem devem ser interpretados como estado atual do usuário, não como auditoria de ataques por período.
- **Severidade depende apenas da quantidade.** O limiar fixo de dez vale para todos os tipos de alerta. É uma decisão implementada e testada, mas não comprova prioridade operacional adequada para cada ocorrência.
- **Há duplicação na montagem de alertas.** As duas versões de `buscarAlertasSistema` repetem mensagens, links e regras; uma correção em apenas uma delas pode produzir divergência entre o endpoint completo e o específico. Extrair a composição comum preservando o reaproveitamento das contagens.
- **Não há snapshot único entre consultas.** Mudanças concorrentes podem produzir diferenças momentâneas entre totais, listas e alertas. Não foi medida a frequência ou relevância desse efeito.
- **Desempenho não foi medido.** O limite de quinze atividades restringe a resposta, mas a consulta reúne dados de três seleções antes da ordenação final. O limite, por si só, não garante leitura barata em tabelas grandes; avaliar com planos e dados representativos.

## Inventário do incremento exclusivo de backend

| Arquivo | Avaliação |
| --- | --- |
| `.idea/compiler.xml` | Acrescenta o módulo `kutuar` ao processamento de anotações; mantém a entrada antiga `synge`. Sem mudança de regra de negócio. |
| `src/main/java/br/com/kutuar/KutuarApp.java` | Instancia o controller e registra as quatro rotas e proteção de Super Admin. |
| `src/main/java/br/com/kutuar/administrativo/controllers/SuperAdminDashboardApiController.java` | Controller enxuto, delega ao serviço e retorna JSON. |
| `src/main/java/br/com/kutuar/administrativo/dto/AlertaSistemaDTO.java` | Transporta tipo, severidade, mensagem, quantidade e link. |
| `src/main/java/br/com/kutuar/administrativo/dto/AtividadeRecenteDTO.java` | Transporta tipo, descrição, data e ID de referência. |
| `src/main/java/br/com/kutuar/administrativo/dto/UltimoAcessoDTO.java` | Transporta usuário, perfil, último login e nome da escola. |
| `src/main/java/br/com/kutuar/administrativo/dto/SuperAdminDashboardResponseDTO.java` | Estende o DTO básico; acrescenta três listas protegidas contra null. Herda a exposição de entidades parciais. |
| `src/main/java/br/com/kutuar/administrativo/repositories/DashboardRepository.java` | Adiciona cinco métodos: últimos acessos, bloqueados, sem acesso recente, tentativas suspeitas e atividades. Usa parâmetros SQL nos limites e no período. |
| `src/main/java/br/com/kutuar/administrativo/services/DashboardService.java` | Compõe resposta, fixa limites, reaproveita contagens e monta alertas; requer correção dos links e melhoria do contrato de usuários recentes. |
| `src/test/java/br/com/kutuar/administrativo/DashboardTest.java` | Acrescenta quatro testes aos três existentes, incluindo garantia de que alertas não chamam consultas desnecessárias. |

## Mudanças herdadas no diff contra origin/main

Além dos dez arquivos acima, seis arquivos aparecem no diff contra o ancestral compartilhado com a referência remota:

| Arquivo | Mudança herdada |
| --- | --- |
| `README.md` | Conteúdo removido. |
| `src/main/java/br/com/kutuar/administrativo/dto/DashboardDTO.java` | Coleções inicializadas e setters protegidos contra null. |
| `src/main/resources/public/css/style.css` | Ajustes visuais, alertas e responsividade. |
| `src/main/resources/public/js/components/navbar.js` | Ajustes de foco, rótulo e mudança de largura para o dashboard. |
| `src/main/resources/templates/dashboard/index.html` | Melhoria de cards, recentes, alertas, ações e gráficos. |
| `src/test/java/br/com/kutuar/administrativo/DashboardPostgresTest.java` | Cinco testes opt-in para as consultas do dashboard básico. |

As otimizações de contagem e séries mensais no repositório/serviço e os três testes unitários originais também são herdados. Eles estão nos mesmos arquivos que receberam o incremento de backend.

As mudanças de Flyway, V2 e componentes reutilizáveis discutidas no relatório de UI são anteriores ao ancestral compartilhado `156a4e8`; não fazem parte do incremento exclusivo de backend nem do diff de três pontos contra a `origin/main` usada nesta análise. Devem ser acompanhadas como problemas herdados, sem atribuí-las ao commit `8d8f4c5`.

## Verificações realizadas

| Verificação | Resultado |
| --- | --- |
| `git diff --check 3ad7b4d..8d8f4c5` | Sem problemas reportados. |
| Compilação da cópia isolada com Java 21 | Sucesso; 188 arquivos de produção compilados. |
| `mvn -o -Dmaven.repo.local=C:\Users\Pichau\.m2\repository -Dtest=DashboardTest,DashboardPostgresTest test` | `BUILD SUCCESS`; **7 testes passaram e 5 foram ignorados**. |
| Teste diagnóstico adicional de serialização | Confirmou status padrão incorreto em `usuariosRecentes`; executado somente na cópia de revisão. |
| Esquema e fluxo de login | Colunas consultadas e persistência de último login verificadas no código. |

Os cinco testes PostgreSQL exigem ativação por `KUTUAR_DASHBOARD_DB_TEST=true` e conexão própria; não foram executados. Além disso, seu conteúdo permanece igual ao da UI: não cobre os cinco métodos de repositório introduzidos no backend.

Os testes adicionados validam severidade nos limites de dez/onze, ausência de alertas quando as contagens são zero, ausência de consultas desnecessárias e preenchimento dos novos campos da resposta completa. O teste cujo nome menciona valores negativos não fornece uma contagem negativa; essa parte do nome excede o cenário efetivamente testado.

Não foram executados a suíte completa, testes HTTP, autorização com tokens reais, integração com PostgreSQL, validação visual ou carga. Os testes unitários aprovados não demonstram correção do SQL nem integração dos links com as telas.

## Próximos passos recomendados

1. Corrigir o contrato JSON de usuários recentes com DTO específico ou mapeamento completo dos campos publicados.
2. Implementar os filtros correspondentes aos links dos alertas e verificar cada destino.
3. Acrescentar testes de integração dos novos métodos SQL e dos quatro endpoints, incluindo acesso anônimo e perfis não autorizados.
4. Definir explicitamente as regras de inatividade, severidade e retenção de histórico.
5. Integrar a API ao dashboard HTML se isso fizer parte da entrega pretendida.

Nenhuma correção funcional foi aplicada à aplicação; foi criado apenas este relatório. O teste diagnóstico foi temporário e restrito à cópia isolada.
