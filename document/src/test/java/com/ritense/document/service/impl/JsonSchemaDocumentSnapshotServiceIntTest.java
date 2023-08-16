/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.document.service.impl;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.WithMockTenantUser;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.service.DocumentDefinitionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
public class JsonSchemaDocumentSnapshotServiceIntTest extends BaseIntegrationTest {

    private JsonSchemaDocumentDefinition definition;
    private JsonSchemaDocument document;
    @Inject
    private DocumentDefinitionService documentDefinitionService;

    @BeforeEach
    public void beforeEach() {
        definition = definition();
        documentDefinitionService.store(definition);
        documentDefinitionService.putDocumentDefinitionRoles(definition.id().name(), Set.of(USER));
        document = (JsonSchemaDocument) createDocument("{\"street\": \"Funenpark\"}");
    }

    @AfterEach
    public void afterEach() {
        documentSnapshotRepository.deleteAll();
    }

    @Test
    @WithMockTenantUser
    public void shouldGetDocumentSnapshots() {
        final var page = documentSnapshotService.getDocumentSnapshots(
            definition.id().name(),
            document.id(),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(1),
            Pageable.unpaged()
        );

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    @WithMockTenantUser
    public void shouldCreateSnapshotWhenCreatingDocument() {

        final var document = (JsonSchemaDocument) createDocument("{}");
        final var page = documentSnapshotService.getDocumentSnapshots(
            null,
            document.id(),
            null,
            null,
            Pageable.unpaged()
        );

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent().get(0).id()).isNotNull();
        assertThat(page.getContent().get(0).snapshotCreatedOn()).isBetween(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        assertThat(page.getContent().get(0).snapshotCreatedBy()).isEqualTo(USERNAME);
        assertThat(page.getContent().get(0).document().id().toString()).hasToString(document.id().toString());
        assertThat(page.getContent().get(0).document().content().asJson().toString()).hasToString(document.content().asJson().toString());
    }

    @Test
    @WithMockTenantUser
    public void shouldCreateSnapshotWhenModifyingDocument() {
        final var request = new ModifyDocumentRequest(
            document.id().toString(),
            new JsonDocumentContent("{\"street\": \"Kanaalkade\"}").asJson(),
            document.version().toString()
        ).withTenantId(TENANT_ID);

        final var modifiedDocument = (JsonSchemaDocument) documentService.modifyDocument(request)
            .resultingDocument().orElseThrow();
        final var page = documentSnapshotService.getDocumentSnapshots(
            null,
            modifiedDocument.id(),
            null,
            null,
            Pageable.unpaged()
        );

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent().get(0).document().content().asJson().toString())
            .isIn(document.content().asJson().toString(), modifiedDocument.content().asJson().toString());
        assertThat(page.getContent().get(1).document().content().asJson().toString())
            .isIn(document.content().asJson().toString(), modifiedDocument.content().asJson().toString());
    }

    private Document createDocument(String content) {
        var result = documentService.createDocument(
            new NewDocumentRequest(
                definition.id().name(),
                new JsonDocumentContent(content).asJson()
            ).withTenantId(TENANT_ID)
        );
        return result.resultingDocument().orElseThrow();
    }

}