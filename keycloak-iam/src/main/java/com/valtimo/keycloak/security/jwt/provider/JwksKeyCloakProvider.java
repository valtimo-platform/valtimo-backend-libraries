package com.valtimo.keycloak.security.jwt.provider;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;

import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.RESOURCE_ACCESS;

public class JwksKeyCloakProvider implements SecretKeyProvider {

    private Logger LOG = LoggerFactory.getLogger(JwksKeyCloakProvider.class);

    private final JWKSet jwkSet;

    public JwksKeyCloakProvider(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }


    @Override
    public boolean supports(SignatureAlgorithm algorithm, Claims claims) {
        return algorithm.isRsa() && (claims.containsKey(REALM_ACCESS) || claims.containsKey(RESOURCE_ACCESS));
    }

    @Override
    public Key getKey(SignatureAlgorithm algorithm, String kId) {
        RSAKey rsaKey = this.jwkSet.getKeyByKeyId(kId).toRSAKey();
        if (rsaKey != null) {
            try {
                return rsaKey.toRSAPublicKey();
            } catch (JOSEException e) {
                LOG.error(String.format("cannot get key for keyId %s", kId));
                throw new RuntimeException(String.format("error in retrieving public key for given keyId %s", kId), e);
            }
        }
        throw new IllegalStateException("Not able to return a key");
    }
}
