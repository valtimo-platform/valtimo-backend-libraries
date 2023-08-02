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

package com.ritense.document.web.rest;

import com.ritense.document.BaseTest;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentSnapshotService;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentSnapshotResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JsonSchemaDocumentSnapshotResourceTest extends BaseTest {

    private JsonSchemaDocumentSnapshotService documentSnapshotService;
    private DocumentSnapshotResource documentSnapshotResource;
    private MockMvc mockMvc;
    private JsonSchemaDocument document;
    private JsonSchemaDocumentSnapshot documentSnapshot;
    private Page<JsonSchemaDocumentSnapshot> documentSnapshotPage;
    private JsonSchemaDocumentDefinition documentDefinition;
    private DocumentDefinitionService<JsonSchemaDocumentDefinition> documentDefinitionService;

    @BeforeEach
    public void setUp() {
        documentSnapshotService = mock(JsonSchemaDocumentSnapshotService.class);
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        documentSnapshotResource = new JsonSchemaDocumentSnapshotResource(documentSnapshotService, documentDefinitionService);

        mockMvc = MockMvcBuilders.standaloneSetup(documentSnapshotResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        document = createDocument(new JsonDocumentContent("{\"street\": \"Funenpark\"}"));
        documentDefinition = definition();
        documentSnapshot = new JsonSchemaDocumentSnapshot(document, LocalDateTime.now(), "user", documentDefinition);
        documentSnapshotPage = new PageImpl<>(List.of(documentSnapshot), Pageable.unpaged(), 1);
    }

    @Test
    public void shouldReturnOkWithDocumentSnapshot() throws Exception {
        when(documentSnapshotService.findById(eq(documentSnapshot.getId())))
            .thenReturn(Optional.of(documentSnapshot));
        when(documentDefinitionService.currentUserCanAccessDocumentDefinition(documentDefinition.id().name()))
            .thenReturn(true);

        mockMvc.perform(get("/api/v1/document-snapshot/{id}", documentSnapshot.id())
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldReturnOkWithDocumentSnapshotPage() throws Exception {
        var fromDateTime = LocalDateTime.now().minusYears(1);
        var toDateTime = LocalDateTime.now().plusYears(1);

        when(documentSnapshotService.getDocumentSnapshots(
            eq(document.definitionId().name()),
            eq(documentSnapshot.document().id()),
            eq(fromDateTime),
            eq(toDateTime),
            any())
        ).thenReturn(documentSnapshotPage);

        mockMvc.perform(get("/api/v1/document-snapshot/")
            .param("definitionName", document.definitionId().name())
            .param("documentId", document.id().toString())
            .param("fromDateTime", fromDateTime.toString())
            .param("toDateTime", toDateTime.toString())
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

}
