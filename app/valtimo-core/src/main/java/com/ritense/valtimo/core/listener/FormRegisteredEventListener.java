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

package com.ritense.valtimo.core.listener;

import com.ritense.form.domain.event.FormsAutoDeploymentFinishedEvent;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.service.FormAssociationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FormRegisteredEventListener {

    private final FormAssociationService formAssociationService;

    public FormRegisteredEventListener(FormAssociationService formAssociationService) {
        this.formAssociationService = formAssociationService;
    }

    // When the first event is published we can assume forms are loaded.
    @EventListener(FormsAutoDeploymentFinishedEvent.class)
    public void handle(FormsAutoDeploymentFinishedEvent event) {

        //////////////
        // Leningen //
        //////////////
        formAssociationService.createFormAssociation(
            "lening-aanvragen",
            "start-form-lening-aanvragen",//name of form
            "start-event",//id of xml element start event
            FormAssociationType.START_EVENT
        );
        formAssociationService.createFormAssociation(
            "lening-aanvragen",
            "user-task-lening-aanvragen", // name of form
            "akkoord-op-lening-task", // id of xml element
            FormAssociationType.USER_TASK
        );
        formAssociationService.createFormAssociation(
            "mail-process",
            "input-email-user-task", // name of form
            "start-event", // id of xml element
            FormAssociationType.START_EVENT
        );

        ////////////////
        // Medewerker //
        ////////////////
        formAssociationService.createFormAssociation(
            "nieuwe-medewerker",
            "start-form-nieuwe-medewerker", // name of form
            "start-event-new-medewerker", // id of xml element
            FormAssociationType.START_EVENT
        );

        ////////////////////
        // Verlofaanvraag //
        ////////////////////
        formAssociationService.createFormAssociation(
            "verlofaanvraag",
            "start-form-verlofaanvraag", // name of form
            "start-event-verlofaanvraag", // id of xml element
            FormAssociationType.START_EVENT
        );

        formAssociationService.createFormAssociation(
            "verlofaanvraag",
            "user-task-akkoord-administratie", // name of form
            "akkoord-verlof", // id of xml element user task
            FormAssociationType.USER_TASK
        );

        //////////////////////////
        // Meldingen (OpenZaak) //
        //////////////////////////
        formAssociationService.createFormAssociation(
            "melding-behandelen",
            "start-form-melding", // name of form
            "start-event-melding-behandelen", // id of xml element user task
            FormAssociationType.START_EVENT
        );

        formAssociationService.createFormAssociation(
            "melding-behandelen",
            "user-task-melding-bekijken", // name of form
            "melding-bekijken", // id of xml element user task
            FormAssociationType.USER_TASK
        );

        ////////////////////
        // Portal Person  //
        ////////////////////
        formAssociationService.createFormAssociation(
            "portal-person",
            "form-portal-voornaam", // name of form
            "view-portal-case", // id of xml element user task
            FormAssociationType.USER_TASK
        );
        formAssociationService.createFormAssociation(
            "portal-person",
            "form-portal-voornaam", // name of form
            "verify-name", // id of xml element user task
            FormAssociationType.USER_TASK
        );
    }
}