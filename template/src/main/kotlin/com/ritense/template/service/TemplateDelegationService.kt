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

package com.ritense.template.service

import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.service.TemporaryResourceStorageService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class TemplateDelegationService(
    private val templateService: TemplateService,
    private val storageService: TemporaryResourceStorageService,
    private val processDocumentService: ProcessDocumentService,
) {

    fun generate(execution: DelegateExecution, templateKey: String, processVariable: String, vararg metadata: String) {
        val document = processDocumentService.getDocument(
            CamundaProcessInstanceId(execution.processInstanceId),
            execution
        )
        val content = templateService.generate(templateKey, document, execution.variables).byteInputStream()

        val metadataMap: MutableMap<String, Any> = (0 until metadata.size / 2)
            .associate { metadata[it * 2] to metadata[it * 2 + 1] }
            .toMutableMap()
        metadataMap[MetadataType.FILE_SIZE.key] = content.available()

        val resourceId = storageService.store(content, metadataMap)
        execution.setVariable(processVariable, resourceId)
    }
}
