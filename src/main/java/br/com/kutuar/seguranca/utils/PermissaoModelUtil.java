package br.com.kutuar.seguranca.utils;

import br.com.kutuar.seguranca.enums.Permissao;
import br.com.kutuar.seguranca.models.AuthUser;
import org.thymeleaf.context.Context;

import java.util.Map;

public final class PermissaoModelUtil {

    private PermissaoModelUtil() {
    }

    public static void popularPermissoes(Context context, AuthUser user) {
        context.setVariable("currentUser", user);
        context.setVariable("canCreateSchool", hasPermission(user, Permissao.ESCOLA_CRIAR));
        context.setVariable("canEditSchool", hasPermission(user, Permissao.ESCOLA_EDITAR));
        context.setVariable("canBlockSchool", hasPermission(user, Permissao.ESCOLA_BLOQUEAR));
        context.setVariable("canViewSchool", hasPermission(user, Permissao.ESCOLA_VISUALIZAR));
        context.setVariable("canCreateUser", hasPermission(user, Permissao.USUARIO_CRIAR));
        context.setVariable("canEditUser", hasPermission(user, Permissao.USUARIO_EDITAR));
        context.setVariable("canBlockUser", hasPermission(user, Permissao.USUARIO_BLOQUEAR));
        context.setVariable("canApproveUser", hasPermission(user, Permissao.USUARIO_APROVAR));
        context.setVariable("canViewUser", hasPermission(user, Permissao.USUARIO_VISUALIZAR));
        context.setVariable("canViewFinance", hasPermission(user, Permissao.FINANCEIRO_VISUALIZAR));
        context.setVariable("canViewAudit", hasPermission(user, Permissao.AUDITORIA_VISUALIZAR));
    }

    public static void popularPermissoes(Map<String, Object> model, AuthUser user) {
        model.put("currentUser", user);
        model.put("canCreateSchool", hasPermission(user, Permissao.ESCOLA_CRIAR));
        model.put("canEditSchool", hasPermission(user, Permissao.ESCOLA_EDITAR));
        model.put("canBlockSchool", hasPermission(user, Permissao.ESCOLA_BLOQUEAR));
        model.put("canViewSchool", hasPermission(user, Permissao.ESCOLA_VISUALIZAR));
        model.put("canCreateUser", hasPermission(user, Permissao.USUARIO_CRIAR));
        model.put("canEditUser", hasPermission(user, Permissao.USUARIO_EDITAR));
        model.put("canBlockUser", hasPermission(user, Permissao.USUARIO_BLOQUEAR));
        model.put("canApproveUser", hasPermission(user, Permissao.USUARIO_APROVAR));
        model.put("canViewUser", hasPermission(user, Permissao.USUARIO_VISUALIZAR));
        model.put("canViewFinance", hasPermission(user, Permissao.FINANCEIRO_VISUALIZAR));
        model.put("canViewAudit", hasPermission(user, Permissao.AUDITORIA_VISUALIZAR));
    }

    private static boolean hasPermission(AuthUser user, Permissao permission) {
        return user != null && user.getPerfil() != null && user.getPerfil().hasPermission(permission);
    }
}
