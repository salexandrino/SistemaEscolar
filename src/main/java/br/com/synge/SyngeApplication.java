package br.com.synge;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyngeApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(SyngeApplication.class);

    public static void main(String[] args) {

        logger.info("Iniciando SYNGE...");

        Javalin app = Javalin.create();

        app.get("/ping", ctx -> ctx.result("pong"));

        app.start(7000);

        logger.info("Servidor iniciado na porta 7000.");
    }
}