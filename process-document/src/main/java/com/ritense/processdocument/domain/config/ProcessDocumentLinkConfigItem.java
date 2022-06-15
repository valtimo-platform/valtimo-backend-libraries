/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.domain.config;

import com.ritense.processdocument.domain.ProcessDocumentDefinition;

public class ProcessDocumentLinkConfigItem {
    private String processDefinitionKey;
    private Boolean canInitializeDocument;
    private Boolean startableByUser;
    private Boolean processIsVisibleInMenu;

    public ProcessDocumentLinkConfigItem() {
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public Boolean getCanInitializeDocument() {
        return canInitializeDocument;
    }

    public boolean canInitializeDocument() {
        return Boolean.TRUE.equals(canInitializeDocument);
    }

    public void setCanInitializeDocument(Boolean canInitializeDocument) {
        this.canInitializeDocument = canInitializeDocument;
    }

    public Boolean getStartableByUser() {
        return startableByUser;
    }

    /**
     * The default is true
     */
    public boolean isStartableByUser() {
        return startableByUser == null || Boolean.TRUE.equals(startableByUser);
    }

    public void setStartableByUser(Boolean startableByUser) {
        this.startableByUser = startableByUser;
    }

    public Boolean getProcessIsVisibleInMenu() {
        return processIsVisibleInMenu;
    }

    public void setProcessIsVisibleInMenu(Boolean processIsVisibleInMenu) {
        this.processIsVisibleInMenu = processIsVisibleInMenu;
    }

    public boolean equalsProcessDocumentDefinition(ProcessDocumentDefinition processDocumentDefinition) {
        return processDocumentDefinition.processDocumentDefinitionId().processDefinitionKey().toString().equals(getProcessDefinitionKey())
                && processDocumentDefinition.startableByUser() == isStartableByUser()
                && processDocumentDefinition.canInitializeDocument() == canInitializeDocument();
    }

    @Override
    public String toString() {
        return "ProcessDocumentLinkConfigItem{" +
                "processDefinitionKey='" + processDefinitionKey + '\'' +
                ", canInitializeDocument=" + canInitializeDocument +
                ", startableByUser=" + startableByUser +
                ", processIsVisibleInMenu=" + processIsVisibleInMenu +
                '}';
    }
}
