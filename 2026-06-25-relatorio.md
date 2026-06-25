# Relatório de Avaliação — EQ14 (APS)

| | |
|---|---|
| **Data** | 2026-06-25 |
| **Repositório** | https://github.com/aps-poo-ufpb/eq14 |
| **Aplicação** | https://aps-eq14.aps.rodrigor.com |
| **Período de atividade** | 2026-06-23 → 2026-06-25 |
| **Total de commits** (sem merges, branch main) | 2 |
| **Integrantes** | Mirela Ronze Felipe Dos Santos (@mirelaronze), Sthefanny Lara Silva Alexandrino (@sthefannyLara-blip) |

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

| Usuário | Commits (main) | Commits (GitHub API) | Linhas adicionadas | Linhas no código atual | % código atual |
|---------|---------------|---------------------|-------------------|----------------------|----------------|
| Mirela Ronze Felipe Dos Santos (@mirelaronze) | 0 | **0** | 0 | 0 | 0% |
| Sthefanny Lara Silva Alexandrino (@sthefannyLara-blip) | 0 | **3** | 0 | 0 | 0% |
| *(sem login GitHub)* | 2 | 100% | — | — | — |

### Contribuição por Camada

| Camada | Total linhas | Mirela Ronze Felipe Dos Santos (@mirelaronze) | Sthefanny Lara Silva Alexandrino (@sthefannyLara-blip) |
|--------|-------------|---------|---------|
| Controller | 33 | 0% | 0% |
| Service | 13 | 0% | 0% |

---

## 5. Contribuição por Funcionalidade

Baseado em `git blame` nos arquivos de controller e service.

| Arquivo | Total linhas | Mirela Ronze Felipe Dos Santos (@mirelaronze) | Sthefanny Lara Silva Alexandrino (@sthefannyLara-blip) |
|---------|-------------|---------|---------|
| `PingController.java` | 20 | 0% | 0% |
| `SyngeApplication.java` | 13 | 0% | 0% |
| `HomeController.java` | 13 | 0% | 0% |

---

*Relatório gerado automaticamente em 2026-06-25.*
*Os dados de contribuição são baseados em `git log --numstat` (linhas adicionadas) e `git blame` (linhas no código atual), excluindo commits de merge.*