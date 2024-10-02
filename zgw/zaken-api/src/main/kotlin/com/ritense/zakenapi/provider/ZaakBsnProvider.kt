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

package com.ritense.zakenapi.provider

import com.ritense.logging.withLoggingContext
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.delegate.DelegateTask
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class ZaakBsnProvider(
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val pluginService: PluginService
) : BsnProvider {

    override fun getBurgerServiceNummer(task: DelegateTask): String? {
        return withLoggingContext(CamundaTask::class, task.id) {
            val documentId =
                processDocumentService.getDocumentId(CamundaProcessInstanceId(task.processInstanceId), task)
            val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId.id).zaakInstanceUrl

            val zakenPlugin = checkNotNull(
                pluginService.createInstance(ZakenApiPlugin::class.java, ZakenApiPlugin.findConfigurationByUrl(zaakUrl))
            ) { "No plugin configuration was found for zaak with URL $zaakUrl" }

            zakenPlugin.getZaakRollen(zaakUrl, RolType.INITIATOR).firstNotNullOfOrNull {
                when (it.betrokkeneIdentificatie) {
                    is RolNatuurlijkPersoon -> it.betrokkeneIdentificatie.inpBsn
                    else -> null
                }
            }
        }
    }
}
