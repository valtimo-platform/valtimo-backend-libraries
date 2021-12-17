/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.domain.mapping.impl

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.openzaak.domain.event.EigenschappenSetEvent
import com.ritense.openzaak.domain.event.ResultaatSetEvent
import com.ritense.openzaak.domain.event.StatusSetEvent
import com.ritense.openzaak.repository.converter.UriAttributeConverter
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import com.ritense.valtimo.contract.domain.AggregateRoot
import com.ritense.valtimo.contract.domain.DomainEvent
import com.ritense.valtimo.contract.validation.Validatable
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.net.URI
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "zaak_type_link")
data class ZaakTypeLink(

    @EmbeddedId
    @JsonProperty("id")
    val zaakTypeLinkId: ZaakTypeLinkId,

    @Column(name = "document_definition_name", columnDefinition = "VARCHAR(50)", nullable = false)
    @field:Length(max = 50)
    @field:NotBlank
    val documentDefinitionName: String,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "zaak_type_url", columnDefinition = "VARCHAR(512)", nullable = false)
    var zaakTypeUrl: URI,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "service_task_handlers", columnDefinition = "json")
    var serviceTaskHandlers: ServiceTaskHandlers

) : Persistable<ZaakTypeLinkId>, Validatable, AggregateRoot<DomainEvent>() {

    init {
        validate()
    }

    fun changeZaakTypeUrl(zaakTypeUrl: URI) {
        this.zaakTypeUrl = zaakTypeUrl
    }

    fun assignZaakServiceHandler(request: ServiceTaskHandlerRequest) {
        serviceTaskHandlers.removeIf { handler -> handler.processDefinitionKey == request.processDefinitionKey && handler.serviceTaskId == request.serviceTaskId }
        serviceTaskHandlers.plusAssign(
            ServiceTaskHandler(
                request.processDefinitionKey,
                request.serviceTaskId,
                request.operation,
                request.parameter
            )
        )
    }

    fun removeZaakServiceHandler(processDefinitionKey: String, serviceTaskId: String) {
        serviceTaskHandlers.removeIf { handler -> handler.processDefinitionKey == processDefinitionKey && handler.serviceTaskId == serviceTaskId }
    }

    @JsonIgnore
    fun assignZaakInstanceStatus(zaakInstanceUrl: URI, statusType: URI) {
        registerEvent(StatusSetEvent(zaakInstanceUrl, statusType))
    }

    @JsonIgnore
    fun assignZaakInstanceResultaat(zaakInstanceUrl: URI, resultaatType: URI) {
        registerEvent(ResultaatSetEvent(zaakInstanceUrl, resultaatType))
    }

    @JsonIgnore
    fun assignZaakInstanceEigenschappen(zaakInstanceLink:ZaakInstanceLink, eigenschappen: MutableMap<URI, String>) {
        registerEvent(
            EigenschappenSetEvent(
                zaakInstanceLink.zaakInstanceUrl,
                zaakInstanceLink.zaakInstanceId,
                eigenschappen
            )
        )
    }

    @JsonIgnore
    override fun getId(): ZaakTypeLinkId {
        return zaakTypeLinkId
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return zaakTypeLinkId.isNew
    }

    @JsonIgnore
    private fun getServiceTaskHandlerBy(processDefinitionKey: String, serviceTaskId: String): ServiceTaskHandler? {
        return serviceTaskHandlers.find { handler -> handler.processDefinitionKey == processDefinitionKey && handler.serviceTaskId == serviceTaskId }
    }

    @JsonIgnore
    fun handleServiceTask(execution: DelegateExecution, processDefinitionKey: String, zaakInstanceUrl: URI?) {
        val serviceTaskId = execution.currentActivityId
        val serviceTaskHandler = getServiceTaskHandlerBy(processDefinitionKey, serviceTaskId)

        if (serviceTaskHandler != null) {
            when (serviceTaskHandler.operation) {
                Operation.SET_STATUS -> {
                    assignZaakInstanceStatus(zaakInstanceUrl!!, serviceTaskHandler.parameter)
                }
                Operation.SET_RESULTAAT -> {
                    assignZaakInstanceResultaat(zaakInstanceUrl!!, serviceTaskHandler.parameter)
                }
            }
        }
    }

    @JsonIgnore
    fun isCreateZaakTask(execution: DelegateExecution, processDefinitionKey: String): Boolean {
        val serviceTaskId = execution.currentActivityId
        val serviceTaskHandler = getServiceTaskHandlerBy(serviceTaskId, processDefinitionKey) ?: return false

        return serviceTaskHandler.operation == Operation.CREATE_ZAAK
    }

}