# Relatório de Avaliação — EQ14 (APS)

| | |
|---|---|
| **Data** | 2026-06-25 |
| **Repositório** | https://github.com/aps-poo-ufpb/eq14 |
| **Aplicação** | https://aps-eq14.aps.rodrigor.com |
| **Período de atividade** | 2026-06-23 → 2026-06-23 |
| **Total de commits** (sem merges) | 1 |
| **Integrantes** |  |

---

## 1. Tecnologias

- Spring Boot 3.5.4
- Spring Security

---

## 2. Análise Funcional

### Endpoints REST (2 mapeados)

| Método | Path | Arquivo |
|--------|------|---------|
| `GET` | `/` | `HomeController.java` |
| `GET` | `/ping` | `PingController.java` |

---

## 3. Análise Arquitetural

| Aspecto | Status | Observação |
|---------|--------|-----------|
| Arquitetura em camadas | ❌ | controller=✅  service=❌  repository=❌ |
| Testes automatizados | ❌ | 0 arquivo(s) de teste |
| Migrations versionadas | ❌ | não encontradas |
| Logging | ❌ | não detectado |
| Autenticação / Segurança | ✅ | Spring Security / JWT / decorator detectado |
| DTOs / Separação de dados | ❌ | não detectado |
| Tratamento global de exceções | ❌ | não detectado |
| Documentação de API (OpenAPI) | ❌ | não detectado |
| Variáveis de ambiente | ❌ | não detectado |
| Dockerfile / docker-compose | ✅ | presente |

### Conformidade com requisitos da disciplina (APS)

| Requisito | Status |
|-----------|--------|
| Javalin | ❌ |
| Thymeleaf | ❌ |
| PostgreSQL | ✅ (provisionado pelo servidor) |
| Testes automatizados | ❌ |

---

## 4. Contribuição por Usuário

### Resumo

| Usuário | Commits | % commits | Linhas adicionadas | Linhas no código atual | % código atual |
|---------|---------|-----------|-------------------|----------------------|----------------|
| *(sem login GitHub)* | 1 | 100% | — | — | — |

---

*Relatório gerado automaticamente em 2026-06-25.*
*Os dados de contribuição são baseados em `git log --numstat` (linhas adicionadas) e `git blame` (linhas no código atual), excluindo commits de merge.*