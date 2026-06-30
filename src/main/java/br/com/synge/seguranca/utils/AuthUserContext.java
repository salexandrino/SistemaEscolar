package br.com.synge.seguranca.utils;

import br.com.synge.seguranca.models.AuthUser;

public class AuthUserContext {
    private static final ThreadLocal<AuthUser> currentUser = new ThreadLocal<>();

    public static void setAuthUser(AuthUser user) {
        currentUser.set(user);
    }

    public static AuthUser getAuthUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
