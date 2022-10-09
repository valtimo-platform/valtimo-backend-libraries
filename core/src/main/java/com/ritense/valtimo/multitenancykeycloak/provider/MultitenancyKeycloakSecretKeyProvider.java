package com.ritense.valtimo.multitenancykeycloak.provider;

import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import com.ritense.valtimo.multitenancy.service.CurrentTenantService;
import com.ritense.valtimo.multitenancykeycloak.domain.KeycloakRealm;
import com.ritense.valtimo.multitenancykeycloak.domain.TenantKeycloakConfig;
import com.ritense.valtimo.multitenancykeycloak.service.TenantKeycloakConfigService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class MultitenancyKeycloakSecretKeyProvider implements SecretKeyProvider {
    public final static String REALM_ACCESS = "realm_access";
    public final static String RESOURCE_ACCESS = "resource_access";
    private final TenantKeycloakConfigService tenantKeycloakConfigService;

    private final WebClient webClient;

    public MultitenancyKeycloakSecretKeyProvider(
        TenantKeycloakConfigService tenantKeycloakConfigService,
        WebClient webClient
    ) {
        this.tenantKeycloakConfigService = tenantKeycloakConfigService;
        this.webClient = webClient;
    }

    @Override
    public boolean supports(SignatureAlgorithm algorithm, Claims claims) {
        return algorithm.isRsa() && (claims.containsKey(REALM_ACCESS) || claims.containsKey(RESOURCE_ACCESS));
    }

    @Override
    public Key getKey(SignatureAlgorithm algorithm) {
        TenantKeycloakConfig tenantKeycloakConfig = getKeycloakConfig(
            CurrentTenantService.getCurrentTenant()
        );

        try {
            return getPublicKey(algorithm, getKeycloakRealmPublicKey(tenantKeycloakConfig));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private TenantKeycloakConfig getKeycloakConfig(String tenantId) {
        return tenantKeycloakConfigService.findByTenantId(tenantId);
    }

    private byte[] getKeycloakRealmPublicKey(TenantKeycloakConfig keycloakConfig) {
        String uri =
            keycloakConfig.getKeycloakServerUrl() +
                "/realms/" +
            keycloakConfig.getKeycloakRealm();

        KeycloakRealm keycloakRealm = webClient
            .get()
            .uri(uri)
            .retrieve()
            .toEntity(KeycloakRealm.class)
            .block()
            .getBody();

        assert keycloakRealm != null;
        return Base64.decodeBase64(keycloakRealm.getPublicKey());
    }

    private RSAPublicKey getPublicKey(SignatureAlgorithm algorithm, byte[] key) throws GeneralSecurityException {
        final KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getFamilyName());
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(key));
    }
}
