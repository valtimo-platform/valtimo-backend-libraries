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

package com.ritense.form.web.rest.impl;

import com.ritense.form.web.rest.FormFileResource;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.resource.FileStatus;
import com.ritense.valtimo.contract.resource.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

public class FormIoFormFileResource implements FormFileResource {

    private final ResourceService resourceService;

    public FormIoFormFileResource(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    @PostMapping(value = "/form-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<? extends Resource> uploadFile(
        @RequestParam("name") String fileName,
        @RequestParam("file") MultipartFile file
    ) {
        Resource storedResource = resourceService.store(fileName, file, FileStatus.PENDING);
        return ResponseEntity.ok(storedResource);
    }

    @Override
    @PostMapping(value = "/form-file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<? extends Resource> uploadOpenZaakFile(
        @RequestParam("documentDefinitionName") String documentDefinitionName,
        @RequestParam("name") String fileName,
        @RequestParam("file") MultipartFile file
    ) {
        Resource storedResource = resourceService.store(documentDefinitionName, fileName, file);
        return ResponseEntity.ok(storedResource);
    }

    @Override
    @GetMapping(value = "/form-file")
    public RedirectView getFile(@RequestParam("form") String fileName) {
        return new RedirectView(
            resourceService
                .getResourceUrl(stripInitialSlashFromPath(fileName))
                .toString()
        );
    }

    @Override
    @DeleteMapping(value = "/form-file")
    public ResponseEntity<Void> deleteFile(@RequestParam("form") String fileName) {
        //TODO needs update of s3 object with tag archive instead of direct deletion
        resourceService.removeResource(stripInitialSlashFromPath(fileName));
        return ResponseEntity.noContent().build();
    }

    private String stripInitialSlashFromPath(String input) {
        return input.replaceFirst("^\\/{1}", "");
    }
}