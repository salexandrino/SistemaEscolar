package br.com.synge;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public class SyngeApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(SyngeApplication.class);

    public static void main(String[] args) {

        logger.info("Iniciando SYNGE...");

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );

        Javalin app = Javalin.create();

        app.get("/", ctx -> ctx.result("SYNGE está no ar!"));

        app.get("/ping", ctx -> ctx.json(Map.of(
                "status", "ok",
                "service", "eq14",
                "timestamp", Instant.now().toString()
        )));

        app.start(port);

        logger.info("Servidor iniciado na porta {}", port);
    }
}