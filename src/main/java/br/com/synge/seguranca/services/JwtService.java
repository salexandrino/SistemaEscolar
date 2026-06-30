package br.com.synge.seguranca.services;

import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.models.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.github.cdimascio.dotenv.Dotenv;
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
    private final long jwtExpirationMinutes; // Duração do JWT em minutos

    public JwtService() {
        Dotenv dotenv = Dotenv.load();
        String secretString = dotenv.get("JWT_SECRET");

        if (secretString == null || secretString.isEmpty()) {
            logger.error("JWT_SECRET não configurado.");
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
        }

        this.jwtExpirationMinutes =
                Long.parseLong(dotenv.get("JWT_EXPIRATION_MINUTES", "1440"));
    }

    public String gerarToken(UUID userId, UUID tenantId, Perfil perfil, String cpf) {
        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES));

        String token = Jwts.builder()
                .claim("userId", userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("escolaId", tenantId.toString()) // Adiciona escolaId ao token
                .claim("perfil", perfil.name())
                .claim("cpf", cpf)
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
                    .verifyWith((javax.crypto.SecretKey) secretKey)
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
            UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
            UUID escolaId = UUID.fromString(claims.get("escolaId", String.class)); // Extrai escolaId
            Perfil perfil = Perfil.valueOf(claims.get("perfil", String.class));
            String cpf = claims.get("cpf", String.class);
            return new AuthUser(userId, tenantId, perfil, cpf);
        } catch (Exception e) {
            logger.error("Erro ao extrair AuthUser dos claims do JWT: {}", e.getMessage());
            return null;
        }
    }
}