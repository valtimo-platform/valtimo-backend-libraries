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

package com.ritense.gzac;

import com.ritense.valtimo.contract.event.ResourceDeployRequestedEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestResource {

    private final ApplicationEventPublisher applicationEventPublisher;

    public TestResource(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/api/management/v1/redeploy")
    public String test() throws IOException {
        String source = "src/main/resources";
        File srcDir = new File(source);

        String destination = "build/resources/main";
        File destDir = new File(destination);

        this.applicationEventPublisher.publishEvent(new ResourceDeployRequestedEvent());

        FileUtils.copyDirectory(srcDir, destDir);
        return new String(getResourceAsStream("test.txt").readAllBytes(), StandardCharsets.UTF_8);
    }

    private static InputStream getResourceAsStream(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }
}
