# SYNGE — Guia de Testes e Avaliação

Este guia foi escrito para quem **nunca viu o projeto antes** (ex.: o professor
avaliando a entrega). Ele explica como rodar o sistema, como entrar como
administrador e como testar os principais fluxos (CRUDs).

---

## 1. Como executar o projeto

### Pré-requisitos

- **Java 21** (JDK)
- **Maven 3.9+**
- **PostgreSQL 17** (ou compatível — o projeto usa Flyway para gerenciar o schema)
- Uma IDE (recomendado: IntelliJ IDEA) ou apenas terminal + `mvn`

### Configurar as variáveis de ambiente

O projeto lê a configuração de um arquivo `.env` na raiz (usando a lib `dotenv-java`).
Crie um arquivo `.env` com este conteúdo, ajustando para o seu ambiente local:

```
DB_URL=jdbc:postgresql://localhost:5432/synge
DB_USER=postgres
DB_PASSWORD=postgres
PORT=8080
JWT_SECRET=uma-string-longa-e-aleatoria-aqui
```

> **Importante:** se `JWT_SECRET` não for definido, a aplicação gera uma chave
> aleatória a cada vez que é reiniciada — isso faz qualquer usuário logado
> perder a sessão a cada restart do servidor. Sempre defina essa variável.

Crie o banco vazio antes de iniciar (`CREATE DATABASE synge;`) — o Flyway cuida
de criar todas as tabelas e dados iniciais automaticamente na primeira execução.

### Como iniciar a aplicação

Pela IDE: rode a classe `br.com.synge.SyngeApplication` (método `main`).

Pelo terminal:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="br.com.synge.SyngeApplication"
```

Se tudo der certo, o terminal mostra:
```
Javalin started in ...ms \o/
Listening on http://localhost:8080/
```

Se a aplicação **encerrar sozinha** logo no início com uma mensagem de erro
crítico do Flyway, é porque alguma migração falhou — o app foi propositalmente
configurado para **não subir** com um banco em estado inconsistente (fail-fast),
em vez de rodar escondendo o problema.

### Como acessar o sistema

Abra `http://localhost:8080` no navegador. A partir da landing page:
- **"Entrar"** → login de usuários comuns (CPF + senha).
- **Botão com ícone de escudo** (no canto superior direito) → login exclusivo
  do Super Administrador (e-mail + senha) — ver seção abaixo.

---

## 2. Login de Super Administrador

### Por que existe um login separado para o admin?

O SYNGE é multi-tenant: cada usuário comum pertence a uma escola (tenant) e só
enxerga dados da própria escola. O **Super Administrador** é a única conta que
enxerga e gerencia **todas as escolas e todos os usuários do sistema** — por
isso ele não se encaixa no formulário de cadastro/login normal (que sempre
exige vincular o usuário a uma escola existente).

Separamos o login dele em uma rota própria (`/super-admin/login`), com um
formulário mais simples (e-mail + senha, sem CPF nem escola), para deixar claro
que essa é uma conta de administração do sistema como um todo, e não de uma
escola específica.

### Objetivo do usuário Super Admin

- Cadastrar e gerenciar escolas (ativar/inativar, editar dados).
- Aprovar ou rejeitar o cadastro de novos usuários pendentes.
- Ter visão completa do sistema (dashboard administrativo).

### Como usá-lo para testar

O Super Admin já vem **pré-cadastrado automaticamente** pela migração Flyway
`V8__seed_super_admin.sql` — não é preciso cadastrá-lo manualmente.

**Email:**
```
synge.gestao@gmail.com
```

**Senha:**
```
SuperAdmin@123
```

Acesse `http://localhost:8080/super-admin/login` e entre com essas credenciais.

> **Se o login falhar com "senha incorreta" em um banco recém-criado do zero:**
> isso é um problema conhecido — o hash gravado pela migração pode estar
> dessincronizado da senha em texto puro acima (aconteceu durante o
> desenvolvimento). Se acontecer, gere um novo hash rodando este trecho uma
> única vez (por exemplo, temporariamente dentro do `main()`) e depois remova-o:
> ```java
> String novoHash = new PasswordService().hash("SuperAdmin@123");
> // UPDATE usuario SET senha_hash = '<novoHash>' WHERE email = 'synge.gestao@gmail.com';
> ```

---

## 3. Como testar os CRUDs

Depois de logar como Super Admin, você cai no **Dashboard** (`/dashboard`),
com um menu lateral. Existem dois CRUDs principais:

### CRUD de Escolas (`/dashboard/escolas`)

- **Listar:** a tela já abre mostrando todas as escolas cadastradas, com busca
  e paginação.
- **Cadastrar:** botão "+ Nova Escola" → preencha nome e CNPJ (obrigatórios) →
  salvar.
- **Visualizar:** ícone de olho na linha da escola → mostra os dados completos,
  somente leitura.
- **Editar:** ícone de lápis → altera os dados e tem botões para
  **ativar/inativar** a escola.

### CRUD de Usuários (`/dashboard/usuarios`)

- **Listar:** mostra todos os usuários, com status (ativo/pendente) e perfil.
- **Cadastrar:** um usuário novo se cadastra sozinho pela tela pública
  `/cadastro` (não pelo painel admin) — ele entra com status **pendente**.
- **Aprovar:** na listagem, usuários pendentes têm um botão de aprovação — só
  depois de aprovado o usuário consegue fazer login.
- **Editar:** ícone de lápis → altera nome, e-mail, telefone, perfil e (agora)
  data de nascimento.
- **Visualizar:** ícone de olho → dados completos, somente leitura.
- **Alterar perfil / Remover:** disponíveis como ações na listagem.

### Fluxo sugerido de teste ponta a ponta

1. Logue como Super Admin.
2. Cadastre uma escola nova em `/dashboard/escolas/nova`.
3. Abra `/cadastro` em uma aba anônima e registre um usuário vinculado a essa
   escola (ele fica pendente).
4. Volte ao painel admin, em `/dashboard/usuarios`, e aprove esse usuário.
5. Faça login com o usuário recém-aprovado em `/login` (CPF + senha) e
   confirme que o acesso funciona.

---

## 4. Documentação da API (Swagger / OpenAPI)

O projeto usa **Javalin**, não Spring Boot — por isso a documentação da API
**não usa `springdoc-openapi`** (essa biblioteca é específica do Spring e não
funciona em um projeto Javalin puro). Em vez disso, foi adicionada uma
especificação **OpenAPI 3.0 manual**, compatível com qualquer stack, mais uma
interface Swagger UI estática (carregada via CDN, sem dependências novas no
back-end):

- Especificação: `http://localhost:8080/openapi.yaml`
- Interface visual: `http://localhost:8080/swagger-ui.html`

Ambos os arquivos ficam em `src/main/resources/public/` e são servidos
automaticamente pelo Javalin (que já expõe essa pasta como estática) — não foi
necessário criar nenhuma rota nova no back-end para isso.
