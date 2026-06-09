package com.gymsocial.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.user.User;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class JwtService {

    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);

    private static final String ISSUER = "gym-social-api";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final PublicIdCodec publicIdCodec;

    public JwtService(String secret, PublicIdCodec publicIdCodec) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.publicIdCodec = publicIdCodec;
        this.verifier = JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build();
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();

        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(publicIdCodec.encode(user.id()))
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(now)
            .withExpiresAt(now.plus(ACCESS_TOKEN_DURATION))
            .sign(algorithm);
    }

    public long verifyAccessToken(String token) {
        String subject = verifier.verify(token).getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Token subject is missing");
        }

        return publicIdCodec.decode(subject)
            .orElseThrow(() -> new IllegalArgumentException(
                "Token subject is invalid"
            ));
    }
}
