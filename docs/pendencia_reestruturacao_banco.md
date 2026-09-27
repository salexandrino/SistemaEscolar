# Pendência técnica: consolidação do modelo de tenant

O banco em uso é exclusivamente de desenvolvimento e será recriado do zero antes do deploy em produção.

Antes da criação do banco definitivo, revisar e consolidar o modelo de multi-tenancy:

- definir claramente e documentar os papéis de `escola.id` e `escola.tenant_id`;
- revisar todas as tabelas que possuem `tenant_id`, suas relações e a forma como o tenant é propagado;
- validar desde o início as FKs, os índices, as constraints, o isolamento multi-tenant e os seeds;
- projetar o schema final de modo que a criação limpa do banco aplique esse modelo de forma consistente.

As migrations históricas de realinhamento, incluindo a V4 já aplicada no ambiente de desenvolvimento, não devem ser tratadas como a estratégia definitiva para produção. Não é necessário criar migration adicional para preservar dados históricos neste momento: a recriação futura do banco deverá partir do modelo consolidado.
