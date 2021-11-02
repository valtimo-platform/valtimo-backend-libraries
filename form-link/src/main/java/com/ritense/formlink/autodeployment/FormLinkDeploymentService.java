/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ritense.form.domain.Mapper;
import com.ritense.formlink.service.FormAssociationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class FormLinkDeploymentService {

    public static final String PATH = "classpath*:config/formlink/*.json";
    private final ResourceLoader resourceLoader;
    private final FormAssociationService formAssociationService;

    void deployAllFromResourceFiles() {
        logger.info("Deploying all form links from {}", PATH);
        try {
            final Resource[] resources = loadResources();
            for (Resource resource: resources){
                var formLinks = getJson(IOUtils.toString(resource.getInputStream(), UTF_8));

                for (SimpleFormLink formLink :formLinks){
                    formAssociationService.createFormAssociation(
                        getNameFromFileName(resource.getFilename()),
                        formLink.getFormName(),
                        formLink.getFormLinkElementId(),
                        formLink.getFormAssociationType()
                    );
                }
            }
        } catch (IOException e) {
            logger.error("Error while deploying form-links", e);
        }
    }

    private List<SimpleFormLink> getJson(String rawJson) throws JsonProcessingException {
        TypeReference<List<SimpleFormLink>> typeRef = new TypeReference<>() {};
        return Mapper.INSTANCE.get().readValue(rawJson, typeRef);
    }

    private String getNameFromFileName(String filename) {
        return filename.substring(0, filename.length() - 5);
    }

    private Resource[] loadResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH);
    }

}