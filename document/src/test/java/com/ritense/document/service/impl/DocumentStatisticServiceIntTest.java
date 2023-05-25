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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.DocumentStatisticService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;

import javax.transaction.Transactional;
import java.util.Set;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("integration")
@Transactional
class DocumentStatisticServiceIntTest extends BaseIntegrationTest {

    private static final String USER_ID = "a28994a3-31f9-4327-92a4-210c479d3055";

    @Autowired
    private DocumentStatisticService documentStatisticService;

    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void beforeEach() {
        documentRepository.deleteAll();
        definition = definition();
        documentDefinitionService.putDocumentDefinitionRoles(definition.id().name(), Set.of(USER));

        var user = new ValtimoUserBuilder().username(USERNAME).email(USERNAME).id(USER_ID).build();
        when(userManagementService.findById(USER_ID)).thenReturn(user);
        when(userManagementService.getCurrentUser()).thenReturn(user);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldReturnUnassignedDocumentCount() {
        // run without authorization
        var document1 = createDocument(definition, "{}");
        createDocument(definition, "{}");
        documentService.assignUserToDocument(document1.id().getId(), USER_ID);

        var unassignedDocumentCountDtos = documentStatisticService.getUnassignedDocumentCountDtos();

        // run without authorization

        assertThat(
            AuthorizationContext.getWithoutAuthorization(
                () -> documentService.getAllByDocumentDefinitionName(
                    Pageable.unpaged(),
                    "house"
                    ).getTotalElements()
            )
        ).isEqualTo(2);
        assertThat(unassignedDocumentCountDtos).hasSizeGreaterThanOrEqualTo(1);
        var unassignedHouseCountDto = unassignedDocumentCountDtos
            .stream()
            .filter(
                dto -> dto.getDocumentDefinitionName().equals("house")
            ).toList().get(0);
        assertThat(unassignedHouseCountDto.getDocumentDefinitionName()).isEqualTo("house");
        assertThat(unassignedHouseCountDto.getOpenDocumentCount()).isEqualTo(1);
    }

}
