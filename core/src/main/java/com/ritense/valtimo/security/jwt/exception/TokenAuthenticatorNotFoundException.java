package com.ritense.valtimo.security.jwt.exception;

public class TokenAuthenticatorNotFoundException extends RuntimeException{

    public TokenAuthenticatorNotFoundException(String message) {
        super(message);
    }
}
