package com.ritense.openzaak.provider

import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNietNatuurlijkPersoon
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.delegate.DelegateTask
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class ZaakKvkProvider(
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val zaakRolService: ZaakRolService
) : KvkProvider {

    override fun getKvkNummer(task: DelegateTask): String? {
        val document = processDocumentService
            .getDocument(CamundaProcessInstanceId(task.processInstanceId), task)
        val zaakLink = zaakInstanceLinkService.getByDocumentId(document.id().id)
        return zaakRolService.getZaakInitator(zaakLink.zaakInstanceUrl)
            .results
            .firstNotNullOfOrNull {
                when(it.betrokkeneIdentificatie) {
                    is RolNietNatuurlijkPersoon -> it.betrokkeneIdentificatie.annIdentificatie
                    else -> null
                }
            }
    }
}
