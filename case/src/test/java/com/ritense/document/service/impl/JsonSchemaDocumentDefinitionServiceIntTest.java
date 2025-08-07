/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ritense.BaseIntegrationTest;
import com.ritense.authorization.permission.ConditionContainer;
import com.ritense.authorization.permission.Permission;
import com.ritense.authorization.permission.condition.FieldPermissionCondition;
import com.ritense.authorization.permission.condition.PermissionConditionOperator;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@Transactional
class JsonSchemaDocumentDefinitionServiceIntTest extends BaseIntegrationTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldNotFindVersionsForNonExistingDocumentDefinitionName() {
        final var versions = documentDefinitionService.findVersionsByName("nothing");
        assertThat(versions)
            .isEmpty();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = USER)
    void shouldGetForRolesOnly() {
        final var expectedDefinition = runWithoutAuthorization(() -> documentDefinitionService.findActiveByName("house")).get();

        final var unexpectedDefinition = runWithoutAuthorization(() -> documentDefinitionService.findActiveByName("person")).get();

        permissionRepository.save(new Permission(
            UUID.randomUUID(),
            JsonSchemaDocumentDefinition.class,
            JsonSchemaDocumentDefinitionActionProvider.VIEW_LIST,
            new ConditionContainer(List.of(
                new FieldPermissionCondition<>(
                    "id.name",
                    PermissionConditionOperator.EQUAL_TO,
                    "house"
                )
            )),
            roleRepository.findByKey(USER)
        ));

        final var all = (List<DocumentDefinition>) documentDefinitionService.findAll(Pageable.unpaged()).getContent();
        assertThat(all).contains(expectedDefinition);
        assertThat(all).doesNotContain(unexpectedDefinition);
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = ADMIN)
    void shouldGetAllForAdmin() {
        assertThatThrownBy(() -> documentDefinitionService.findAllForManagement(Pageable.unpaged()))
            .isExactlyInstanceOf(AccessDeniedException.class);

        final var all = (List<DocumentDefinition>) runWithoutAuthorization(() -> documentDefinitionService.findAllForManagement(Pageable.unpaged())).toList();
        assertThat(all).hasSizeGreaterThanOrEqualTo(7);
    }
}