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

package com.ritense.form.autodeployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.form.BaseIntegrationTest;
import com.ritense.form.service.FormLoaderService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class FormDefinitionDeploymentServiceIntTest extends BaseIntegrationTest {

    @Inject
    private ResourceLoader resourceLoader;

    @Inject
    private FormLoaderService formLoaderService;

    /**
     * Other tests delete the form definitions from the database, causing this test to fail depending on the order the tests are ran in.
     * For now we disable this test, but we should fix this at some point, preferably by running this test in a new/different application instance.
     */
    @Test
    @Disabled("See JavaDoc")
    public void deployAllFromResourceFiles() throws IOException {
        Resource[] resources = loadResources();
        for (Resource resource : resources) {
            Optional<JsonNode> formIoFormDefinitions = formLoaderService
                .getFormDefinitionByName(getNameFromFileName(resource.getFilename()));
            assertThat(formIoFormDefinitions).isPresent();
        }
    }

    private String getNameFromFileName(String filename) {
        return filename.substring(0, filename.length() - 5);
    }

    private Resource[] loadResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(FormDefinitionDeploymentService.PATH);
    }
}