package com.ritense.zakenapi.provider

import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.delegate.DelegateTask
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class ZaakKvkProvider(
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val pluginService: PluginService
) : KvkProvider {

    override fun getKvkNummer(task: DelegateTask): String? {
        val documentId = processDocumentService.getDocumentId(CamundaProcessInstanceId(task.processInstanceId), task)
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId.id).zaakInstanceUrl

        val zakenPlugin = checkNotNull(
            pluginService.createInstance(ZakenApiPlugin::class.java, ZakenApiPlugin.findConfigurationByUrl(zaakUrl))
        ) { "No plugin configuration was found for zaak with URL $zaakUrl" }

        return zakenPlugin.getZaakRollen(zaakUrl, RolType.INITIATOR).firstNotNullOfOrNull {
            when (it.betrokkeneIdentificatie) {
                is RolNietNatuurlijkPersoon -> it.betrokkeneIdentificatie.annIdentificatie
                else -> null
            }
        }
    }
}
