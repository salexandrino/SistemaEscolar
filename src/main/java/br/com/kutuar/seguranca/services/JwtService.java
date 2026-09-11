package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.models.AuthUser;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final long jwtExpirationMinutes;

    public JwtService() {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String secretString = System.getenv("JWT_SECRET");

        if (secretString == null || secretString.isBlank()) {
            secretString = dotenv.get("JWT_SECRET");
        }

        if (secretString == null || secretString.isBlank()) {
            logger.error("JWT_SECRET não configurado.");
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
        }

        String expirationMinutes = System.getenv("JWT_EXPIRATION_MINUTES");

        if (expirationMinutes == null || expirationMinutes.isBlank()) {
            expirationMinutes = dotenv.get("JWT_EXPIRATION_MINUTES", "1440");
        }

        jwtExpirationMinutes = Long.parseLong(expirationMinutes);
    }

    public String gerarToken(UUID userId,
                             UUID tenantId,
                             UUID escolaId,
                             Perfil perfil,
                             String cpf) {

        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES));

        JwtBuilder builder = Jwts.builder()
                .claim("userId", userId.toString())
                .claim("perfil", perfil.name())
                .claim("cpf", cpf);

        if (tenantId != null) {
            builder.claim("tenantId", tenantId.toString());
        }

        if (escolaId != null) {
            builder.claim("escolaId", escolaId.toString());
        }

        String token = builder
                .setIssuedAt(Date.from(now))
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        logger.info("Token JWT gerado para userId: {}", userId);

        return token;
    }

    public Jws<Claims> validarToken(String token) {

        try {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

        } catch (Exception e) {

            logger.warn("Falha na validação do token JWT: {}", e.getMessage());

            return null;
        }
    }

    public AuthUser extrairAuthUser(String token) {

        Jws<Claims> jws = validarToken(token);

        if (jws == null) {
            return null;
        }

        Claims claims = jws.getBody();

        try {

            UUID userId = UUID.fromString(claims.get("userId", String.class));

            String tenantIdStr = claims.get("tenantId", String.class);
            UUID tenantId = tenantIdStr != null
                    ? UUID.fromString(tenantIdStr)
                    : null;

            String escolaIdStr = claims.get("escolaId", String.class);
            UUID escolaId = escolaIdStr != null
                    ? UUID.fromString(escolaIdStr)
                    : null;

            Perfil perfil = Perfil.valueOf(claims.get("perfil", String.class));

            String cpf = claims.get("cpf", String.class);

            return new AuthUser(
                    userId,
                    tenantId,
                    escolaId,
                    perfil,
                    cpf
            );

        } catch (Exception e) {

            logger.error("Erro ao extrair AuthUser dos claims do JWT", e);

            return null;
        }
    }
}