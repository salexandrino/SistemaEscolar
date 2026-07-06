# Como resolver: banco sem tabelas / migrations não aplicadas

**Equipe 16 — APS (SIGFA)**
**Diagnóstico feito em:** 2026-07-06 (Prof. Rodrigo)

> Este documento explica **por que** o banco de produção está sem tabelas e traz o
> **passo a passo** para corrigir. Leiam o diagnóstico antes de aplicar — o problema
> **não** é "faltou subir migration para o git": as migrations estão no repositório.

---

## Diagnóstico (o que realmente acontece)

O app sobe e responde `/ping`, mas **nenhuma tabela é criada** no banco (`escola`,
`usuario` não existem; `flyway_schema_history` fica vazia). Qualquer operação real
falha — o log de produção mostra:

```
ERROR FlywayConfig - Erro ao aplicar Flyway: No database found to handle jdbc:postgresql://.../eq14
...
ERROR UsuarioRepository - relation "usuario" does not exist
```

São **três** causas somadas:

1. **Fat-jar sem o handler de PostgreSQL do Flyway (causa principal).**
   O `pom.xml` declara `flyway-database-postgresql`, mas o `maven-shade-plugin` só
   usa o `ManifestResourceTransformer`. Sem o **`ServicesResourceTransformer`**, ao
   montar o jar único os arquivos `META-INF/services/*` do `flyway-core` e do
   `flyway-database-postgresql` **colidem e um sobrescreve o outro**, apagando o
   registro do módulo PostgreSQL. Em runtime o Flyway não reconhece a URL
   `jdbc:postgresql://` → `migrate()` lança exceção → **nenhuma migration roda**.

2. **`FlywayConfig` sabota banco novo.** As opções
   `baselineVersion("4")` + `baselineOnMigrate(true)` fazem o Flyway **pular V1–V4** —
   inclusive a **V1, que cria as tabelas**. Mesmo consertando o item 1, num banco
   limpo as tabelas continuariam não sendo criadas.

3. **`JWT_SECRET` não configurado.** A autenticação quebra mesmo com o banco OK.

---

## Passo 1 — Corrigir o empacotamento do fat-jar (`pom.xml`)

No bloco do `maven-shade-plugin`, **adicione** o `ServicesResourceTransformer` junto
do transformer que já existe:

```xml
<transformers>
    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
        <mainClass>br.com.synge.SyngeApplication</mainClass>
    </transformer>
    <!-- ADICIONAR: mescla os META-INF/services (senão o módulo postgres do Flyway some) -->
    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
</transformers>
```

Esse transformer **mescla** (em vez de sobrescrever) os arquivos de serviço, mantendo
o handler de PostgreSQL do Flyway no jar. É a correção da causa principal.

---

## Passo 2 — Corrigir o `FlywayConfig.java`

Arquivo: `src/main/java/br/com/synge/config/FlywayConfig.java`

**Antes:**
```java
Flyway flyway = Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .baselineVersion("4")      // Mantém a base simulada para estabilizar na nuvem
        .validateOnMigrate(false)
        .load();

logger.info("Executando Flyway Repair original...");
flyway.repair();
```

**Depois:**
```java
Flyway flyway = Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .baselineVersion("0")      // banco novo => aplica TUDO a partir da V1
        .validateOnMigrate(true)   // volta a validar (recomendado)
        .load();

flyway.migrate();
```

Pontos importantes:
- `baselineVersion("0")` (ou remover a linha) faz a **V1 rodar** e criar as tabelas.
- Reative `validateOnMigrate(true)` — ele protege contra migration alterada depois de aplicada.
- **Remova o `flyway.repair()`** da inicialização normal. `repair()` foi acrescentado
  para mascarar o sintoma; com o build correto ele não é necessário. (Use só
  manualmente se algum dia der checksum mismatch.)

---

## Passo 3 — Configurar o `JWT_SECRET`

O app precisa da variável de ambiente `JWT_SECRET` (o log acusa
`JWT_SECRET não configurado`). No repositório, garanta que ela está documentada no
`.env.example`, e no deploy ela precisa ser injetada pelo servidor.

Avisem o professor que a eq16 precisa de `JWT_SECRET` no `.env` de produção — ele
adiciona a variável no ambiente do servidor (o valor não vai para o git).

---

## Passo 4 — Faxina nas migrations (opcional, mas recomendado)

- **Gap da V9:** a sequência vai V1–V8 e pula para V10/V11. O Flyway tolera o buraco
  (aplica o que existe), então **não quebra** — mas renumerem V10→V9 e V11→V10 para
  manter a ordem limpa, **ou** deixem como está de forma consciente.
- **Cópia perdida:** existe um `db/migration/V5__ensure_escola_columns.sql` na **raiz**
  do repositório, fora de `src/main/resources`. Ele não entra no classpath (é lixo).
  **Apaguem** para evitar confusão.

---

## Passo 5 — Rebuild, redeploy e verificação

1. Faça `git push` das correções na `main` (o workflow builda e faz deploy).
2. Como o banco `eq14` está **vazio de tabelas** e a `flyway_schema_history` está
   zerada, no próximo deploy o Flyway (já corrigido) vai aplicar **V1..V11** e criar
   tudo automaticamente — **não é preciso apagar o banco**.
3. Verifique no log de deploy que aparece algo como
   `Successfully applied N migrations` (e **não** mais `No database found to handle`).
4. Teste o login do super admin em `https://eq16.aps.rodrigor.com` — não deve mais dar
   `relation "usuario" does not exist`.

---

## Checklist rápido

- [ ] `ServicesResourceTransformer` adicionado no shade (Passo 1)
- [ ] `baselineVersion("0")` + `validateOnMigrate(true)` e `repair()` removido (Passo 2)
- [ ] `JWT_SECRET` documentado e solicitado ao professor (Passo 3)
- [ ] V9 renumerada e cópia perdida da V5 apagada (Passo 4)
- [ ] Push → deploy → log mostra migrations aplicadas → login funciona (Passo 5)
