package com.ritense.objectsapi.taak.initiator

import com.ritense.objectsapi.taak.ProcessDocumentService
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import org.camunda.bpm.engine.delegate.DelegateTask

class DocumentInitiatorProvider(
    private val processDocumentService: ProcessDocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val zaakRolService: ZaakRolService
): BsnProvider, KvkProvider {


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