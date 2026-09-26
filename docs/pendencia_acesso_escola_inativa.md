# Pendência: acesso de usuários de escola inativa

O status da escola foi padronizado como `ATIVA` ou `INATIVA`; `INATIVA` é o bloqueio administrativo da escola e não há estado `BLOQUEADA` separado.

O cadastro público já recusa escolas inativas. Porém, a autenticação atual valida apenas o estado do usuário e não consulta o status da escola no login. Assim, bloquear o login de gestor, professor e secretaria de uma escola inativa requer uma decisão de produto sobre sessões/JWT já emitidos e deve ser implementado em uma alteração própria.
