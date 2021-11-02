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

package com.ritense.formlink.autodeployment;

import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.service.FormAssociationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.inject.Inject;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class FormLinkDeploymentServiceIntTest extends BaseIntegrationTest {

    @Inject
    private ResourceLoader resourceLoader;

    @Inject
    private FormAssociationService formAssociationService;

    @Test
    public void deployAllFromResourceFiles() throws IOException {
        Resource[] resources = loadResources();
        for (Resource resource : resources) {
            var formAssociationOptional = formAssociationService.getFormAssociationByFormLinkId(
                getNameFromFileName(resource.getFilename()),
                "start-event-bezwaar-proces-gestart"
            );
            assertThat(formAssociationOptional).isPresent();
        }
    }

    private String getNameFromFileName(String filename) {
        return filename.substring(0, filename.length() - 5);
    }

    private Resource[] loadResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(FormLinkDeploymentService.PATH);
    }
}