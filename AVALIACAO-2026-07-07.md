# Avaliação — EQ14 (APS)

**Data:** 2026-07-07  
**Método:** verificação automática lendo `origin/main` + checagem do deploy ao vivo.

> Avaliação automática preliminar. O que não estiver no repositório e no ar é considerado não atendido.

---

## 1. Tecnologias obrigatórias

- ✅ **Docker** — Dockerfile na raiz + docker-compose
- ✅ **Javalin** — dependência no pom + uso no código
  - Evidência: `src/main/java/br/com/synge/SyngeApplication.java:27:import io.javalin.Javalin;`
- ✅ **Thymeleaf** — dependência no pom + templates com th:
  - Evidência: `src/main/resources/templates/auth/cadastro.html:12:    <link rel="stylesheet" th:href="${'/css/style.css'}">`
- ✅ **Bootstrap** — referenciado nos templates/estáticos
  - Evidência: `src/main/resources/templates/auth/cadastro.html:9:    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">`
- ✅ **PostgreSQL** — driver no pom + uso confirmado
  - Evidência: `.env:1:DB_URL=jdbc:postgresql://localhost:5432/synge`

## 2. Estrutura esperada

- ✅ **Padrão Maven** — src/main/java + src/main/resources
- ✅ **Deploy em produção** — container ok; https://eq14.aps.rodrigor.com/ping → HTTP 200
- ✅ **Logs** — framework de log + uso no código
  - Evidência: `src/main/java/br/com/synge/SyngeApplication.java:42:    private static final Logger logger = LoggerFactory.getLogger(SyngeApplication.class);`
- ✅ **Testes automatizados** — 3 classe(s) de teste (JUnit no pom)
- ✅ **Autenticação** — hash de senha usado no código + login/sessão
  - Evidência: `src/main/java/br/com/synge/seguranca/services/PasswordService.java:17:        return BCrypt.hashpw(senha, BCrypt.gensalt(LOG_ROUNDS));`
- ✅ **Autorização** — papéis + verificação de acesso em rotas
  - Evidência: `src/main/java/br/com/synge/SyngeApplication.java:101:        app.before(new AuthMiddleware(jwtService));`
- ✅ **Modularidade** — 10 camadas: config, controller, dto, exception, middleware, model, repository, seguranca, service, util

## 3. Arquitetura e banco de dados

- **Classes por camada:** controller: 5, service: 6, repository: 6, model: 4, dto: 11, config: 2, seguranca: 5, middleware: 4, exception: 8, util: 3 (total 54 classes)
- **Tabelas no banco:** 2 tabela(s): escola, usuario
- **Entidades no código:** 4 entidade(s): AuthUser, Escola, RecuperacaoSenha, Usuario

---

*Gerado automaticamente em 2026-07-07. Requisitos: tecnologias obrigatórias (Docker, Javalin, Thymeleaf, Bootstrap, PostgreSQL), estrutura esperada (padrão Maven, deploy, logs, testes, autenticação, autorização, modularidade), arquitetura em camadas e modelagem do banco.*