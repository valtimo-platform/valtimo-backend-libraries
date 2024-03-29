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

package com.ritense.formlink.domain.impl.formassociation;

import com.ritense.formlink.domain.FormLink;
import com.ritense.formlink.domain.FormLinkTaskProvider;
import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.valtimo.camunda.domain.CamundaTask;

@Deprecated(since = "10.6.0", forRemoval = true)
public class FormFormLinkTaskProvider implements FormLinkTaskProvider<FormTaskOpenResultProperties> {

    private static final String FORM_TASK_TYPE_KEY = "form";

    @Override
    public boolean supports(FormLink formLink) {
        return formLink instanceof BpmnElementFormIdLink;
    }

    @Override
    public TaskOpenResult<FormTaskOpenResultProperties> getTaskResult(CamundaTask task, FormLink formLink) {
        return new TaskOpenResult<>(FORM_TASK_TYPE_KEY, new FormTaskOpenResultProperties(formLink.getId()));
    }
}
