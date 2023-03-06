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

package com.ritense.openzaak.provider

import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNatuurlijkPersoon
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.delegate.DelegateTask
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class ZaakBsnProvider(
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val zaakRolService: ZaakRolService
) : BsnProvider {

    override fun getBurgerServiceNummer(task: DelegateTask): String? {
        val document = processDocumentService.getDocument(CamundaProcessInstanceId(task.processInstanceId), task)
        val zaakLink = zaakInstanceLinkService.getByDocumentId(document.id().id)
        return zaakRolService.getZaakInitator(zaakLink.zaakInstanceUrl)
            .results.firstNotNullOfOrNull {
                when(it.betrokkeneIdentificatie) {
                    is RolNatuurlijkPersoon -> it.betrokkeneIdentificatie.inpBsn
                    else -> null
                }
            }
    }

}
