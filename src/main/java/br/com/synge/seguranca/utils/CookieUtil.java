package br.com.synge.seguranca.utils;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;


public class CookieUtil {

    private static final String JWT_COOKIE_NAME = "JWT_TOKEN";
    private static final int JWT_COOKIE_MAX_AGE_SECONDS = 30 * 24 * 60 * 60; // 30 dias

    public static void addJwtCookie(Context ctx, String token) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
        cookie.setMaxAge(JWT_COOKIE_MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        cookie.setSecure(ctx.req().isSecure()); // Apenas em HTTPS
        cookie.setPath("/");
        cookie.setSameSite(SameSite.LAX);
        ctx.cookie(cookie);    }

    public static String getJwtToken(Context ctx) {
        return ctx.cookie(JWT_COOKIE_NAME);
    }

    public static void removeJwtCookie(Context ctx) {
        Cookie cookie =  new Cookie(JWT_COOKIE_NAME, ""); // Max-Age = 0 para expirar imediatamente
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(ctx.req().isSecure());
        cookie.setPath("/");
        cookie.setSameSite(SameSite.LAX);
        ctx.cookie(cookie);
    }
}
