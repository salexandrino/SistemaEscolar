# Credencial do Super Admin

A migration `V6__corrige_hash_super_admin.sql` contém um hash fixo e deve ser tratada como comprometida. Antes de produção, altere a credencial real do Super Admin diretamente no banco do ambiente, por um canal seguro e controlado. Não use uma migration para definir senha ou hash real.
