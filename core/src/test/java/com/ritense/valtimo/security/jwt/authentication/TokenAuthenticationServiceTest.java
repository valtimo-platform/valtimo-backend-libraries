package com.ritense.valtimo.security.jwt.authentication;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class TokenAuthenticationServiceTest {

    @Test
    void validateToken() {
        var pk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiROOUZS7YMKKTzHp/VuZ0UyxoXIRgUMZCFLW6tbzk0UVW20pj3nxVsG0Be3Zt2jYRGex5vtk4JkYi5x/s5q81wwAcfa2e1vUOFhl6waLfgooLjaw0TeEilTl+qN95Qul3dnWw5ui9Ml8OcydM0DC1dGRDj6a3M7tznIUtFVUpcR3e0nvKzhJx2z7KUgg265q3QHDikpht//74xh6wLrxoalk+TkrrlZcg2Rhe0Kf7TPbIy1xaE9+k/uumLj53SXct0ztmIOoy2xKq2oIDMUAhdVYos9XZX8vrS7Q8+gNUuH91+6yEfUPRhKFinOBNAjtCO2yNqchk+LX+NUCiDFl6wIDAQAB";

        try {
            var key = getPublicKey(SignatureAlgorithm.RS256, Base64.decodeBase64(pk));
            final var builder = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();

            final var claimsJws = builder.parseClaimsJws("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJsdkpZTEljSlNDYmczODBwR3hVb1pST2x3dXdJZ1dKM0U1RlFBZkRxckFFIn0.eyJleHAiOjE2NjM3NTYxNzQsImlhdCI6MTY2Mzc1NTg3NCwianRpIjoiOTZkMzBmMzAtZGUyNy00ZjBmLWJjYTktNjQ4ODk3Y2VmYWEyIiwiaXNzIjoiaHR0cHM6Ly9rZXljbG9hay52YWx0aW1vLm5sL2F1dGgvcmVhbG1zL2FsbG5leC1ub24tcHJvZHVjdGlvbiIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJiOGE4ZDE5OC04NzgxLTQ3NDktYTFlMi1kMzdlNDIwMWMxZDEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvcHRhLWNsaWVudCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJST0xFX09QVEEiLCJST0xFX01BQ0hJTkUiLCJkZWZhdWx0LXJvbGVzLXJlYmVsei1hbGxuZXgtbm9uLXByb2R1Y3Rpb24iLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudElkIjoib3B0YS1jbGllbnQiLCJjbGllbnRIb3N0IjoiNjIuMjguMTkwLjE4NiIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW9wdGEtY2xpZW50IiwiY2xpZW50QWRkcmVzcyI6IjYyLjI4LjE5MC4xODYifQ.hPHJ_-92nkp7cAKXPf49c_Ys63CI0ecB0wLfAYRljTNCQEJHcX1_4ilISHLykK7stKxuYb6c6vlImQF7Ko4A30q9JI1uBFVF-tK_-_IhKQBikq2QrJlFWkbuGHaTPXD3MeNnPyZ6WIvLI3Mlmbl2Umkqr9dqJ6ErcA3sAwCNYoYKUt2BJRQcBL9lPrGXGft8hJeDTTAsE7wdxIRKnq4fGWEDNFyDIWqlB4r6sEqWdY16L0ynq1Q-R-ny8oORRDiExMt9nB-UEiXlQ-BJOEG-4WnZKvDiXJt6yIXoGCjg5MB1EXFFqEfHd81BkdIaI8ykrgPByZ7wLvCUwfxqq-WdoA");
            assertThat(claimsJws).isNull();
        } catch (JwtException ex) {
            fail();
            // we *cannot* use the JWT as intended by its creator
        } catch (GeneralSecurityException e) {
            fail();
        }
    }

    private RSAPublicKey getPublicKey(SignatureAlgorithm algorithm, byte[] key) throws GeneralSecurityException {
        final KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getFamilyName());
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(key));
    }

}