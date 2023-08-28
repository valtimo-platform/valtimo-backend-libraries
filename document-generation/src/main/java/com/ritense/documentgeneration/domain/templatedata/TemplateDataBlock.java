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

package com.ritense.documentgeneration.domain.templatedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TemplateDataBlock {

    private final String name;
    private final List<TemplateDataBlockItem> templateDataBlockItems;

    public TemplateDataBlock(
        String name,
        List<TemplateDataBlockItem> templateDataBlockItems
    ) {
        this.name = name;
        this.templateDataBlockItems = Collections.unmodifiableList(Objects.requireNonNull(templateDataBlockItems));
    }

    public String getName() {
        return name;
    }

    public List<TemplateDataBlockItem> getTemplateDataBlockItems() {
        return templateDataBlockItems;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private final List<TemplateDataBlockItem> templateDataBlockItems;

        private Builder() {
            templateDataBlockItems = new ArrayList<>();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder addDataBlockItem(TemplateDataBlockItem dataBlockItem) {
            templateDataBlockItems.add(dataBlockItem);
            return this;
        }

        public TemplateDataBlock build() {
            return new TemplateDataBlock(name, templateDataBlockItems);
        }
    }

}