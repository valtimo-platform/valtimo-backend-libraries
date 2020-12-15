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

package com.ritense.valtimo.task.publictask;

import com.ritense.valtimo.contract.config.ValtimoProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class PublicTaskTokenService {

    public static final String PROCESS_DEFINITION_KEY = "process_definition_key";
    public static final String FORM_LINK_ID = "form_link_id";
    public static final String DOCUMENT_ID = "document_id";
    public static final String TASK_INSTANCE_ID = "task_instance_id";

    private final ValtimoProperties valtimoProperties;

    public PublicTaskTokenService(ValtimoProperties valtimoProperties) {
        this.valtimoProperties = valtimoProperties;
    }

    public String getTokenForTask(PublicTaskRequest publicTaskRequest) {
        return getTokenForTask(publicTaskRequest, null);
    }

    public String getTokenForTask(PublicTaskRequest publicTaskRequest, String username) {
        final Claims claims = new DefaultClaims();
        claims.setIssuer("ValtimoPublicTask");

        if (username != null) {
            claims.setSubject(username);
        }
        claims.setIssuedAt(new Date());
        claims.put(PROCESS_DEFINITION_KEY, publicTaskRequest.getProcessDefinitionKey());
        claims.put(FORM_LINK_ID, publicTaskRequest.getFormLinkId());
        claims.put(DOCUMENT_ID, publicTaskRequest.getDocumentId());
        claims.put(TASK_INSTANCE_ID, publicTaskRequest.getTaskInstanceId());

        final SecretKey key = Keys.hmacShaKeyFor(valtimoProperties.getPublicTask().getTokenSecret());
        return Jwts.builder()
            .addClaims(claims)
            .signWith(key)
            .compact();
    }

    public PublicTaskRequest getTaskClaims(String jwtToken) throws PublicTaskTokenParseException {
        Claims body;
        try {
            body = Jwts.parserBuilder()
                .setSigningKey(valtimoProperties.getPublicTask().getTokenSecret())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
        } catch (JwtException ex) {
            throw new PublicTaskTokenParseException(ex);
        }

        if (!body.getIssuer().equals("ValtimoPublicTask")) {
            throw new PublicTaskTokenParseException("The issuer did not match \"ValtimoPublicTask\"");
        }

        PublicTaskRequest.PublicTaskRequestBuilder builder = new PublicTaskRequest.PublicTaskRequestBuilder();
        builder.setProcessDefinitionKey(body.get(PROCESS_DEFINITION_KEY, String.class));
        builder.setFormLinkId(body.get(FORM_LINK_ID, String.class));
        builder.setDocumentId(body.get(DOCUMENT_ID, String.class));
        builder.setTaskInstanceId(body.get(TASK_INSTANCE_ID, String.class));

        if (body.getSubject() != null) {
            builder.setUsername(body.getSubject());
        }

        return builder.createPublicTaskTokenClaims();
    }

    public PublicTaskRequest getPublicTaskRequestByAuthorization(String authorizationHeaderValue) throws PublicTaskTokenParseException {
        String jwtToken = authorizationHeaderValue.startsWith("Bearer ") ? authorizationHeaderValue.substring(7) : authorizationHeaderValue;
        return getTaskClaims(jwtToken);
    }
}