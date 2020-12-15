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

package com.ritense.documentgeneration.domain.templatedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TemplateDataBlockItem {

    private final List<TemplateDataField> templateDataFields;
    private final List<TemplateDataBlock> templateDataBlocks;

    public TemplateDataBlockItem(
        List<TemplateDataField> templateDataFields,
        List<TemplateDataBlock> templateDataBlocks
    ) {
        this.templateDataFields = Collections.unmodifiableList(Objects.requireNonNull(templateDataFields));
        this.templateDataBlocks = Collections.unmodifiableList(Objects.requireNonNull(templateDataBlocks));
    }

    public List<TemplateDataField> getTemplateDataFields() {
        return templateDataFields;
    }

    public List<TemplateDataBlock> getTemplateDataBlocks() {
        return templateDataBlocks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<TemplateDataField> templateDataFields;
        private final List<TemplateDataBlock> templateDataBlocks;

        private Builder() {
            templateDataFields = new ArrayList<>();
            templateDataBlocks = new ArrayList<>();
        }

        public Builder addDataField(TemplateDataField templateDataField) {
            templateDataFields.add(templateDataField);
            return this;
        }

        public Builder addDataBlock(TemplateDataBlock templateDataBlock) {
            templateDataBlocks.add(templateDataBlock);
            return this;
        }

        public TemplateDataBlockItem build() {
            return new TemplateDataBlockItem(templateDataFields, templateDataBlocks);
        }
    }

}