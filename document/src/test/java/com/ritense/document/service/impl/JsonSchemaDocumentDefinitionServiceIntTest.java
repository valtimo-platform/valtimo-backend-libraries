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

package com.ritense.document.service.impl;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.impl.JsonSchema;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import javax.transaction.Transactional;
import java.net.URI;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
public class JsonSchemaDocumentDefinitionServiceIntTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldDeployNextVersionWhenDifferentSchema() {
        final var differentHouseSchema = JsonSchema.fromResource(URI.create("config/document/definition/noautodeploy/house_v2.schema.json"));

        documentDefinitionService.deploy(differentHouseSchema);

        final var documentDefinition = documentDefinitionService.findLatestByName("house").get();
        assertThat(documentDefinition.id().version()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldNotDeployNextVersionWhenSchemaAlreadyExists() {
        final var existingHouseSchema = JsonSchema.fromResource(URI.create("config/document/definition/house.schema.json"));

        documentDefinitionService.deploy(existingHouseSchema);

        final var documentDefinition = documentDefinitionService.findLatestByName("house").get();
        assertThat(documentDefinition.id().version()).isEqualTo(1);
    }

}