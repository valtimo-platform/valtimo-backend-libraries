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

package com.ritense.objectsapi.taak

import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.opennotificaties.OpenNotificationEvent
import com.ritense.objectsapi.taak.resolve.ValueResolverService
import com.ritense.valtimo.service.BpmnModelService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.springframework.context.event.EventListener
import java.util.UUID

class TaakObjectListener(
    private val openNotificatieService: OpenNotificatieService,
    private val taskService: TaskService,
    private val valueResolverService: ValueResolverService,
    private val bpmnModelService: BpmnModelService,
) {

    @EventListener(OpenNotificationEvent::class)
    fun notificationReceived(event: OpenNotificationEvent) {
        if (event.notification.kanaal == OpenNotificatieConnector.OBJECTEN_KANAAL_NAME
            && event.notification.isEditNotification()
        ) {
            val connector = openNotificatieService.findConnector(event.connectorId, event.authorizationKey) as TaakObjectConnector
            val taakObjectId = event.notification.getObjectId()
            val taakObject = connector.getTaakObject(taakObjectId)
            if (taakObject.status != TaakObjectStatus.ingediend) {
                return
            }
            val task = getTaskByExecutionId(taakObject.verwerkerTaakId)
            taskService.complete(task.id)

            connector.deleteTaakObject(taakObjectId)
        }
    }

    private fun getTaskByExecutionId(executionId: UUID): Task {
        return taskService.createTaskQuery()
            .executionId(executionId.toString())
            .singleResult()
    }
}