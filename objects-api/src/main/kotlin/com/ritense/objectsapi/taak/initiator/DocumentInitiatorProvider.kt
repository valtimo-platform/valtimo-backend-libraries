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

package com.ritense.objectsapi.taak.initiator

import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import org.camunda.bpm.engine.delegate.DelegateTask

class DocumentInitiatorProvider(
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val zaakRolService: ZaakRolService
) : BsnProvider, KvkProvider {


    override fun getBurgerServiceNummer(task: DelegateTask): String? {
        val document = processDocumentService.getDocument(CamundaProcessInstanceId(task.processInstanceId), task)
        val zaakLink = zaakInstanceLinkService.getByDocumentId(document.id().id)
        return zaakRolService.getZaakInitator(zaakLink.zaakInstanceUrl)
            .results.firstNotNullOfOrNull {
                it.betrokkeneIdentificatie?.inpBsn
            }
    }

    override fun getKvkNummer(task: DelegateTask): String? {
        //TODO: "Not yet implemented"
        return null
    }

}