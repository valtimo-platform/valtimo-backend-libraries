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

package com.ritense.formlink.domain.impl.formassociation;

import com.ritense.formlink.domain.FormLink;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementAngularStateUrlLink;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementUrlLink;

import java.util.UUID;

public class FormAssociationFactory {

    public static CamundaFormAssociation getFormAssociation(
        UUID formAssociationId,
        FormAssociationType type,
        String formLinkElementId,
        UUID formId,
        String customUrl,
        String angularStateUrl,
        Boolean isPublic
    ) {
        if (type == null) {
            throw new RuntimeException("Cannot determine form association");
        }
        final FormLink formLink = getFormLink(formLinkElementId, formId, customUrl, angularStateUrl, isPublic);

        if (type.equals(FormAssociationType.START_EVENT)) {
            return new StartEventFormAssociation(formAssociationId, formLink);
        } else if (type.equals(FormAssociationType.USER_TASK)) {
            return new UserTaskFormAssociation(formAssociationId, formLink);
        }
        throw new RuntimeException("Cannot determine form association");
    }

    private static FormLink getFormLink(
        String formLinkElementId,
        UUID formId,
        String customUrl,
        String angularStateUrl,
        Boolean isPublic
    ) {
        if (formId != null) {
            return new BpmnElementFormIdLink(formLinkElementId, formId, isPublic);
        } else if (customUrl != null) {
            return new BpmnElementUrlLink(formLinkElementId, customUrl, isPublic);
        } else if (angularStateUrl != null) {
            return new BpmnElementAngularStateUrlLink(formLinkElementId, angularStateUrl, isPublic);
        }
        throw new RuntimeException("Cannot determine form link");
    }

}
