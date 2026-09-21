# Relatório de revisão — feat/superadmin-dashboard-ui

Data: 20/09/2026. Commit analisado: `3ad7b4d`.

## Parecer

A branch melhora significativamente a organização do dashboard, substitui atividades ilustrativas por dados reais e acrescenta agregações para os gráficos. Entretanto, a entrega completa ainda precisa de ajustes: os componentes reutilizáveis falham na renderização, a lista de escolas redesenhada não está integrada à rota usada e existe risco de inicialização em bancos que já executaram a versão anterior da migração V2.

**Recomendação: corrigir os achados prioritários e validar a atualização de banco antes de considerar a entrega completa homologada.** Os problemas de migração e componentes já aparecem no ancestral compartilhado com a referência local `origin/main`; portanto, não são todos novidades do próximo merge dessa branch.

## Escopo e método

- A branch ativa no ambiente era `feat/superadmin-dashboard-backend`, com árvore de trabalho inicialmente limpa. A revisão usou uma cópia isolada de `feat/superadmin-dashboard-ui`, sem checkout ou alteração de código de aplicação.
- Comparação histórica: `git diff main...feat/superadmin-dashboard-ui`, cujo ancestral comum é `68a1e06`. A `main` local aponta para `8c65e87`, mas não é ancestral direto da branch; o diff de três pontos parte do ancestral comum.
- Escopo histórico: **9 commits, 40 arquivos, 1.766 linhas adicionadas e 597 removidas**.
- Comparação com a referência remota disponível: `origin/main` em `4f05d72`, ancestral comum `156a4e8`. São **7 commits exclusivos e 9 arquivos no diff de três pontos**, com 460 adições e 263 remoções.
- Não foi realizado `fetch`; as referências remotas são as disponíveis localmente no momento da análise.
- Foram revisados os arquivos alterados e os pontos de integração relevantes: rotas, controllers, modelos, layout e configuração de banco. Foram executados testes do dashboard, verificações de sintaxe JavaScript e renderização Thymeleaf sem banco.
- As referências de arquivo e linha abaixo correspondem ao commit analisado, não necessariamente à branch atualmente aberta no editor.

## Achados prioritários

### 1. Alta — alteração de migração aplicada pode impedir a inicialização

**Local:** `src/main/resources/db/migration/V2__seed_inicial.sql:16` e `src/main/java/br/com/kutuar/config/FlywayConfig.java:31–45`.

A branch troca o hash do administrador dentro de V2 e remove a chamada a `flyway.repair()`, mantendo `validateOnMigrate(true)`. Em um banco que já aplicou a V2 anterior, o conteúdo local deixa de corresponder ao checksum registrado. A validação pode interromper `migrate()`; a configuração relança a exceção e a aplicação chama essa migração na inicialização (`KutuarApp.java:106`).

**Condição:** atualização de um banco com a V2 antiga registrada. Um banco novo não reproduz esse cenário. Não foi feita conexão com banco para reproduzir a falha nesta revisão.

**Correção:** restaurar a migração histórica e tratar qualquer mudança necessária por migração nova ou procedimento administrativo específico. A retirada do repair automático é saudável isoladamente; o problema é combiná-la com a alteração do arquivo já aplicado. Não reintroduzir repair automático como forma genérica de esconder diferenças.

**Origem:** incluído no escopo histórico da branch, já presente no ancestral `156a4e8` compartilhado com `origin/main`.

### 2. Média — componentes novos usam expressões incompatíveis com OGNL

**Local:** `src/main/resources/templates/components/button.html:3–4`; o mesmo padrão aparece em `components/input.html:4` e `components/card.html:1`.

Expressões como `${disabled ?: false}`, `${loading ?: false}` e `${variant ?: 'primary'}` colocam o operador de valor padrão dentro da expressão OGNL. O mecanismo configurado rejeitou essa sintaxe na execução.

**Reprodução realizada:** processamento de `dev/components` e de `layouts/master-admin` com `content=dashboard/escolas/index`. O catálogo falhou em `loading ?: false`; a lista falhou em `disabled ?: false`, inclusive com lista vazia. A causa registrada foi `ognl.ExpressionSyntaxException: Malformed OGNL expression`.

**Impacto:** o catálogo e a página que consome o botão reutilizável não renderizam. O dashboard principal, que usa marcação própria para suas ações, passou na renderização isolada.

**Correção:** usar ternários completos compatíveis, como `${disabled == true}`, ou expressões padrão do Thymeleaf com o operador no nível correto. Revisar todas as ocorrências do padrão nos componentes e verificar o catálogo com dados variados.

**Origem:** já presente no ancestral `156a4e8`; não é uma regressão exclusiva dos sete commits posteriores.

### 3. Média — a lista de escolas redesenhada não é a página servida pela rota

**Local alterado:** `src/main/resources/templates/dashboard/escolas/index.html:1–13`.

**Evidência de integração:** `KutuarApp.java:513` encaminha `/dashboard/escolas` para `EscolaController.exibirPaginaListagem`. Esse método renderiza `dashboard/escolas/lista.html` em `EscolaController.java:625`, e não o `index.html` redesenhado.

**Impacto:** quem acessa “Ver todas” ou o menu de escolas continua no conteúdo antigo. A documentação declara a lista migrada, mas o novo conteúdo não está conectado ao fluxo principal.

**Correção:** aplicar o novo design ao template efetivamente usado ou ajustar o fluxo de renderização. Preservar as funcionalidades da lista atual, incluindo mensagem com credenciais temporárias e ações de ativação/inativação; simplesmente substituir o arquivo pode removê-las.

**Origem:** já presente no ancestral `156a4e8`.

### 4. Média — os novos controles de filtro não executam filtragem

**Local:** `src/main/resources/templates/dashboard/escolas/index.html:6`.

O campo de busca, o seletor de status e a opção “Somente com responsável” não estão conectados a submissão de formulário nem a lógica JavaScript de filtragem. A própria ajuda informa que a busca será conectada futuramente, mas os controles ficam apresentados como interativos.

**Impacto:** mesmo após conectar e corrigir a renderização da página, mudar esses valores não altera a listagem.

**Correção:** implementar a filtragem ou apresentar explicitamente o estado indisponível até que exista comportamento funcional.

**Problema relacionado, preexistente:** os links `?status=ativa` e `?status=inativa`, já existentes no dashboard anterior, chegam ao controller que chama `listarTodas` sem ler esse parâmetro. O novo alerta de escolas inativas também usa esse caminho. Isso deve ser corrigido, mas não foi introduzido originalmente por esta branch.

## Outros pontos de atenção

- **Status da plataforma é estático.** Em `dashboard/index.html:763–803`, “Operacional”, “Banco conectado” e “Serviços disponíveis” não dependem de um resultado de saúde dos serviços. A renderização do dashboard implica que suas consultas funcionaram naquele instante, mas não comprova a disponibilidade dos demais serviços. Ajustar a mensagem ao que é efetivamente medido ou conectar uma verificação de saúde.
- **Acessibilidade do menu ainda é parcial.** `js/components/navbar.js` acrescenta foco inicial, retorno de foco, Escape e atualização de rótulo apenas quando existe `.admin-dashboard`. Não há contenção do foco no menu sobreposto nem isolamento do conteúdo atrás dele. Nas demais páginas, a sidebar mobile é deslocada por CSS sem ser ocultada da navegação por teclado. Validar Tab/Shift+Tab, Escape e navegação com leitor de tela.
- **Limite de 768 px inconsistente fora do dashboard.** `components/navbar.css:3` oculta a sidebar em `max-width:768px`, enquanto o botão usa `d-md-none`, oculto a partir de 768 px pelo Bootstrap utilizado. Exatamente nessa largura, páginas sem o ajuste específico do dashboard podem ficar sem menu visível e sem botão para abri-lo. Harmonizar o breakpoint.
- **Gráficos não têm alternativa textual dos dados.** Os canvases de crescimento e perfis não oferecem tabela ou descrição equivalente. Incluir uma representação acessível dos valores.
- **README foi esvaziado.** A remoção é explícita no último commit e não é defeito de execução, mas elimina a apresentação, a stack e as referências do projeto sem substituto equivalente. Manter ao menos propósito, requisitos e passos de execução.
- **GeradorBCrypt é utilitário de desenvolvimento em código de produção.** Ele contém uma senha fixa e a imprime no console. Não é chamado pela inicialização revisada, portanto não se constatou exposição automática em runtime. Preferir entrada controlada e localização separada do código da aplicação.
- **Manutenção do front-end:** diversos componentes e o layout foram compactados em linhas longas; `style.css` mistura regras legadas, regras globais, compatibilidade e ajustes específicos do dashboard. Isso dificulta localizar conflitos e revisar alterações. Os tokens ajudam, mas ainda há cores e medidas repetidas fora deles.

## Avaliação das mudanças de backend

Os pontos positivos são a contagem de escolas por status em uma consulta, o reaproveitamento da distribuição por perfil para calcular o total de usuários, a seleção explícita de colunas e a ordenação determinística de recentes por data e ID. O limite de cinco registros e `NULLS LAST` tornam o comportamento mais previsível. O DTO inicializa coleções vazias e protege setters contra null.

A série mensal usa uma janela única de seis meses para escolas e usuários, inclui o histórico anterior no primeiro ponto e acumula os valores. O nome de tabela interpolado no SQL está protegido por uma lista permitida (`escola` ou `usuario`), e há teste de rejeição de entrada arbitrária.

O dashboard final executa sete consultas: escolas por status, usuários pendentes, duas listas recentes, duas séries mensais e distribuição por perfil. Em relação à implementação intermediária compartilhada com `origin/main`, a consolidação reduz três consultas. Em relação ao ancestral histórico, o total continua em sete, com mais informação entregue. Não foram coletados tempos, planos de execução ou métricas de carga; portanto, esta revisão confirma a redução estrutural, não uma aceleração medida.

As séries representam a acumulação dos registros atualmente existentes. Exclusões físicas removem registros de pontos históricos recalculados; não se trata de uma trilha histórica imutável. As consultas usam conexões separadas, sem um snapshot único: alterações concorrentes podem produzir pequenas diferenças entre cards e gráficos. São limitações do modelo, não falhas reproduzidas nesta análise.

## Inventário completo dos arquivos alterados

Os caminhos abaixo são relativos à raiz do projeto. Grupos com vários nomes cobrem cada arquivo do diff histórico.

| Arquivo ou grupo | Mudança e avaliação |
| --- | --- |
| `.vscode/settings.json` | Configuração interativa de atualização do build Java; efeito local no editor. |
| `README.md` | Remoção do conteúdo; documentação geral fica vazia. |
| `docs/frontend-design-system.md` | Documenta tokens, componentes, grid e migração; a alegação de lista migrada precisa refletir a rota real. |
| `pom.xml` | Padroniza Java 21 com `release`, reorganiza dependências e comentários; sem atualização das versões das bibliotecas no diff. |
| `src/main/java/br/com/kutuar/administrativo/dto/DashboardDTO.java` | Séries mensais, perfis e coleções protegidas contra null. |
| `src/main/java/br/com/kutuar/administrativo/repositories/DashboardRepository.java` | Agregações, séries acumuladas, dados recentes e tratamento de valores ausentes. |
| `src/main/java/br/com/kutuar/administrativo/services/DashboardService.java` | Composição dos novos dados e período mensal comum. |
| `src/main/java/br/com/kutuar/config/FlywayConfig.java` | Retirada do repair; avaliar junto com a mudança de V2. |
| `src/main/java/br/com/kutuar/seguranca/utils/GeradorBCrypt.java` | Utilitário com senha fixa para gerar/verificar hash. |
| `src/main/resources/db/migration/V2__seed_inicial.sql` | Troca do hash do administrador; risco de checksum em atualização. |
| `src/main/resources/public/css/tokens.css` | Paleta, medidas e aliases de compatibilidade. |
| `src/main/resources/public/css/base.css` | Estilos base, foco e classe para conteúdo visualmente oculto. |
| `src/main/resources/public/css/layout.css` | Estrutura administrativa e grid de 12 colunas. |
| `src/main/resources/public/css/components/button.css`, `card.css`, `input.css`, `checkbox.css`, `dropdown.css`, `navbar.css`, `carousel.css` | Sete arquivos de estilos para os componentes. |
| `src/main/resources/public/css/pages/dashboard.css` | Estilos específicos de dashboard e tabela. |
| `src/main/resources/public/css/style.css` | Agregador por imports, compatibilidade e regras de dashboard/responsividade. |
| `src/main/resources/public/js/app.js` | Ponto de entrada documental; contém apenas comentário. |
| `src/main/resources/public/js/components/carousel.js` | Navegação circular anterior/próximo. |
| `src/main/resources/public/js/components/dropdown.js` | Retorno de foco no Escape; abertura depende do Bootstrap. |
| `src/main/resources/public/js/components/navbar.js` | Abertura/fechamento de sidebar, backdrop e suporte parcial a foco. |
| `src/main/resources/templates/components/button.html`, `card.html`, `input.html`, `checkbox.html`, `dropdown.html`, `navbar.html`, `carousel.html` | Sete fragments reutilizáveis; revisar sintaxe de expressões e validar consumo real. |
| `src/main/resources/templates/dashboard/escolas/index.html` | Nova lista com componentes e filtros visuais, fora da rota principal. |
| `src/main/resources/templates/dashboard/index.html` | Cards, gráficos, recentes, alertas, ações rápidas e status. |
| `src/main/resources/templates/dev/components.html` | Catálogo visual sem rota pública; falha de renderização reproduzida. |
| `src/main/resources/templates/fragments/sidebar.html` | Sidebar reestruturada com classes próprias e agrupamentos. |
| `src/main/resources/templates/layouts/master-admin.html` | Novo layout administrativo, topbar, backdrop e scripts. |
| `src/test/java/br/com/kutuar/administrativo/DashboardTest.java` | Três testes de DTO, composição do serviço e rejeição de tabela arbitrária. |
| `src/test/java/br/com/kutuar/administrativo/DashboardPostgresTest.java` | Cinco testes opt-in com tabelas temporárias e rollback. |

## Verificações e resultados

| Verificação | Resultado |
| --- | --- |
| `git diff --check main...feat/superadmin-dashboard-ui` | Sem problemas de whitespace reportados. |
| `node --check` nos quatro arquivos JavaScript adicionados/alterados | Aprovado; verifica sintaxe, não comportamento de interface. |
| Maven com Java 21, `-Dtest=DashboardTest,DashboardPostgresTest test` | `BUILD SUCCESS`: três testes passaram e cinco foram ignorados. |
| `DashboardPostgresTest` | Não executado: exige `KUTUAR_DASHBOARD_DB_TEST=true` e conexão de banco. |
| Renderização de `layouts/master-admin` com dashboard vazio | Aprovada. |
| Renderização de `dev/components` | Falhou por expressão OGNL no botão. |
| Renderização da nova lista de escolas no layout, vazia e com um registro | Falhou por expressão OGNL no botão nos dois cenários. |

As tentativas iniciais do Maven encontraram resolução offline e restrições de acesso ao cache. Com o cache local correto e acesso autorizado, o build e os testes selecionados concluíram. Essas falhas iniciais são limitações do ambiente, não defeitos atribuídos à branch.

Não foram executados: suíte completa, integração PostgreSQL, atualização de banco existente, servidor com autenticação real, validação visual em navegador ou testes de carga. A responsividade foi avaliada pelo código, sem homologação visual. O teste isolado de renderização não equivale a um teste HTTP ponta a ponta.

## Ordem sugerida de correção

1. Preparar uma atualização de banco que preserve a migração V2 já aplicada.
2. Corrigir as expressões dos componentes e repetir a renderização do catálogo e da lista.
3. Integrar o design à rota real de escolas, preservando suas ações e mensagens atuais.
4. Implementar filtros e alinhar os links do dashboard com o resultado esperado.
5. Validar PostgreSQL em ambiente de teste e os fluxos reais em desktop, tablet e celular.
6. Completar acessibilidade, revisar a mensagem de status e restaurar documentação mínima.

Nenhuma correção funcional foi aplicada durante esta revisão.
