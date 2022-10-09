package com.ritense.valtimo.multitenancykeycloak.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakRealm {
    @JsonProperty("public_key")
    private String publicKey;

    public KeycloakRealm() {
    }

    public String getPublicKey() {
        return publicKey;
    }
}
