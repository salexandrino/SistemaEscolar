package br.com.kutuar.administrativo;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.enums.Permissao;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SidebarRbacTemplateTest {

    @Test
    void sidebarRefletePermissoesDosPerfisExistentes() {
        String superAdmin = renderizar(Perfil.SUPER_ADMIN);
        String gestor = renderizar(Perfil.GESTOR);
        String secretaria = renderizar(Perfil.SECRETARIA);
        String professor = renderizar(Perfil.PROFESSOR);
        String financeiro = renderizar(Perfil.FINANCEIRO);

        assertTrue(superAdmin.contains("/dashboard/escolas/nova"));
        assertTrue(superAdmin.contains("/dashboard/usuarios/novo"));
        assertFalse(gestor.contains("/dashboard/escolas/nova"));
        assertTrue(gestor.contains("/dashboard/usuarios/novo"));
        assertFalse(secretaria.contains("/dashboard/usuarios/novo"));
        assertTrue(secretaria.contains("/dashboard/escolas"));
        assertFalse(professor.contains("menuEscolas"));
        assertFalse(professor.contains("menuUsuarios"));
        assertFalse(financeiro.contains("menuEscolas"));
        assertFalse(financeiro.contains("menuUsuarios"));
    }

    private String renderizar(Perfil perfil) {
        Context context = new Context();
        context.setVariable("canCreateSchool", perfil.hasPermission(Permissao.ESCOLA_CRIAR));
        context.setVariable("canEditSchool", perfil.hasPermission(Permissao.ESCOLA_EDITAR));
        context.setVariable("canBlockSchool", perfil.hasPermission(Permissao.ESCOLA_BLOQUEAR));
        context.setVariable("canViewSchool", perfil.hasPermission(Permissao.ESCOLA_VISUALIZAR));
        context.setVariable("canCreateUser", perfil.hasPermission(Permissao.USUARIO_CRIAR));
        context.setVariable("canEditUser", perfil.hasPermission(Permissao.USUARIO_EDITAR));
        context.setVariable("canBlockUser", perfil.hasPermission(Permissao.USUARIO_BLOQUEAR));
        context.setVariable("canApproveUser", perfil.hasPermission(Permissao.USUARIO_APROVAR));
        context.setVariable("canViewUser", perfil.hasPermission(Permissao.USUARIO_VISUALIZAR));

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine.process("fragments/sidebar", context);
    }
}
