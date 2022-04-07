/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.security.jwt.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.Set;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.NAME_KEY;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE;

public class PublicFormAccessToken implements TokenClaims {

    private String username;
    private Set<String> roles;

    public PublicFormAccessToken(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    @Override
    public Claims getClaims() {
        final Claims claims = new DefaultClaims();
        claims.put(NAME_KEY, this.username);
        claims.put(ROLES_SCOPE, this.roles);
        return claims;
    }

}
