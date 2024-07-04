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

package com.ritense.processdocument.domain.impl;

import com.ritense.processdocument.domain.ProcessDocumentInstance;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import java.time.LocalDateTime;

public class ProcessDocumentInstanceDto implements ProcessDocumentInstance {

    private ProcessDocumentInstanceId id;
    private String processName;
    private boolean isActive;
    private int version;
    private int latestVersion;
    private String startedBy;
    private LocalDateTime startedOn;

    public ProcessDocumentInstanceDto(
        ProcessDocumentInstanceId id,
        String processName,
        boolean isActive
    ) {
        this.id = id;
        this.processName = processName;
        this.isActive = isActive;
    }

    public ProcessDocumentInstanceDto(
        ProcessDocumentInstanceId id,
        String processName,
        boolean isActive,
        int version,
        int latestVersion,
        String startedBy,
        LocalDateTime startedOn
    ) {
        this.id = id;
        this.processName = processName;
        this.isActive = isActive;
        this.version = version;
        this.latestVersion = latestVersion;
        this.startedBy = startedBy;
        this.startedOn = startedOn;
    }

    @Override
    public ProcessDocumentInstanceId processDocumentInstanceId() {
        return id;
    }

    @Override
    public String processName() {
        return processName;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getVersion() {
        return version;
    }

    public int getLatestVersion() {
        return latestVersion;
    }

    public String getStartedBy() {
        return startedBy;
    }

    public LocalDateTime getStartedOn() {
        return startedOn;
    }
}
