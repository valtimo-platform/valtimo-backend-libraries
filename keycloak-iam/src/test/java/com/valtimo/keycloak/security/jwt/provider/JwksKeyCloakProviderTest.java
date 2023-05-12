package com.valtimo.keycloak.security.jwt.provider;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwksKeyCloakProviderTest {

    @Mock
    private JWKSet jwks;
    private JwksKeyCloakProvider jwksKeyCloakProvider;


    @BeforeEach
    void setUp() {
        jwksKeyCloakProvider = new JwksKeyCloakProvider(jwks);
    }

    @Test
    void shouldIndicateSupportRSAAndRealmClaim() {
        Claims claims = new DefaultClaims();
        claims.put(REALM_ACCESS, "value");

        assertTrue(jwksKeyCloakProvider.supports(SignatureAlgorithm.RS256, claims));
    }

    @Test
    void shouldIndicateSupportRSAAndResourceClaim() {
        Claims claims = new DefaultClaims();
        claims.put(RESOURCE_ACCESS, "value");

        assertTrue(jwksKeyCloakProvider.supports(SignatureAlgorithm.RS256, claims));
    }

    @Test
    void shouldIndicateNotSupportHS256() {
        Claims claims = new DefaultClaims();
        claims.put(RESOURCE_ACCESS, "value");
        assertFalse(jwksKeyCloakProvider.supports(SignatureAlgorithm.HS256, claims));
    }

    @Test
    void shouldIndicateNotSupportWhenMissingClaims() {
        Claims claims = new DefaultClaims();
        assertFalse(jwksKeyCloakProvider.supports(SignatureAlgorithm.RS256, claims));
    }

    @Test
    void shouldGetKey() throws NoSuchAlgorithmException {
        String keyId = "aKeyId";

        // Generate the RSA key pair
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

        // Convert to JWK format
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic())
            .privateKey((RSAPrivateKey)keyPair.getPrivate())
            .keyUse(KeyUse.SIGNATURE)
            .keyID(keyId)
            .issueTime(new Date())
            .build();

        when(jwks.getKeyByKeyId(any())).thenReturn(jwk);

        Key key = jwksKeyCloakProvider.getKey(SignatureAlgorithm.RS256, keyId);

        assertEquals("RSA", key.getAlgorithm());
    }
}