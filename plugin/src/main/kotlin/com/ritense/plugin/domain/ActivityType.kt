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

package com.ritense.plugin.domain

import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_CANCEL as CAMUNDA_BOUNDARY_CANCEL
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_COMPENSATION as CAMUNDA_BOUNDARY_COMPENSATION
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_CONDITIONAL as CAMUNDA_BOUNDARY_CONDITIONAL
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_ERROR as CAMUNDA_BOUNDARY_ERROR
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_ESCALATION as CAMUNDA_BOUNDARY_ESCALATION
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_MESSAGE as CAMUNDA_BOUNDARY_MESSAGE
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_SIGNAL as CAMUNDA_BOUNDARY_SIGNAL
import org.camunda.bpm.engine.ActivityTypes.BOUNDARY_TIMER as CAMUNDA_BOUNDARY_TIMER
import org.camunda.bpm.engine.ActivityTypes.CALL_ACTIVITY as CAMUNDA_CALL_ACTIVITY
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_CANCEL as CAMUNDA_END_EVENT_CANCEL
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_COMPENSATION as CAMUNDA_END_EVENT_COMPENSATION
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_ERROR as CAMUNDA_END_EVENT_ERROR
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_ESCALATION as CAMUNDA_END_EVENT_ESCALATION
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_MESSAGE as CAMUNDA_END_EVENT_MESSAGE
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_NONE as CAMUNDA_END_EVENT_NONE
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_SIGNAL as CAMUNDA_END_EVENT_SIGNAL
import org.camunda.bpm.engine.ActivityTypes.END_EVENT_TERMINATE as CAMUNDA_END_EVENT_TERMINATE
import org.camunda.bpm.engine.ActivityTypes.GATEWAY_COMPLEX as CAMUNDA_GATEWAY_COMPLEX
import org.camunda.bpm.engine.ActivityTypes.GATEWAY_EVENT_BASED as CAMUNDA_GATEWAY_EVENT_BASED
import org.camunda.bpm.engine.ActivityTypes.GATEWAY_EXCLUSIVE as CAMUNDA_GATEWAY_EXCLUSIVE
import org.camunda.bpm.engine.ActivityTypes.GATEWAY_INCLUSIVE as CAMUNDA_GATEWAY_INCLUSIVE
import org.camunda.bpm.engine.ActivityTypes.GATEWAY_PARALLEL as CAMUNDA_GATEWAY_PARALLEL
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_CATCH as CAMUNDA_INTERMEDIATE_EVENT_CATCH
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_COMPENSATION_THROW as CAMUNDA_INTERMEDIATE_EVENT_COMPENSATION_THROW
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_CONDITIONAL as CAMUNDA_INTERMEDIATE_EVENT_CONDITIONAL
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_ESCALATION_THROW as CAMUNDA_INTERMEDIATE_EVENT_ESCALATION_THROW
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_LINK as CAMUNDA_INTERMEDIATE_EVENT_LINK
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_MESSAGE as CAMUNDA_INTERMEDIATE_EVENT_MESSAGE
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_MESSAGE_THROW as CAMUNDA_INTERMEDIATE_EVENT_MESSAGE_THROW
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_NONE_THROW as CAMUNDA_INTERMEDIATE_EVENT_NONE_THROW
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_SIGNAL as CAMUNDA_INTERMEDIATE_EVENT_SIGNAL
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_SIGNAL_THROW as CAMUNDA_INTERMEDIATE_EVENT_SIGNAL_THROW
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_THROW as CAMUNDA_INTERMEDIATE_EVENT_THROW
import org.camunda.bpm.engine.ActivityTypes.INTERMEDIATE_EVENT_TIMER as CAMUNDA_INTERMEDIATE_EVENT_TIMER
import org.camunda.bpm.engine.ActivityTypes.MULTI_INSTANCE_BODY as CAMUNDA_MULTI_INSTANCE_BODY
import org.camunda.bpm.engine.ActivityTypes.START_EVENT as CAMUNDA_START_EVENT
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_COMPENSATION as CAMUNDA_START_EVENT_COMPENSATION
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_CONDITIONAL as CAMUNDA_START_EVENT_CONDITIONAL
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_ERROR as CAMUNDA_START_EVENT_ERROR
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_ESCALATION as CAMUNDA_START_EVENT_ESCALATION
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_MESSAGE as CAMUNDA_START_EVENT_MESSAGE
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_SIGNAL as CAMUNDA_START_EVENT_SIGNAL
import org.camunda.bpm.engine.ActivityTypes.START_EVENT_TIMER as CAMUNDA_START_EVENT_TIMER
import org.camunda.bpm.engine.ActivityTypes.SUB_PROCESS as CAMUNDA_SUB_PROCESS
import org.camunda.bpm.engine.ActivityTypes.SUB_PROCESS_AD_HOC as CAMUNDA_SUB_PROCESS_AD_HOC
import org.camunda.bpm.engine.ActivityTypes.TASK as CAMUNDA_TASK
import org.camunda.bpm.engine.ActivityTypes.TASK_BUSINESS_RULE as CAMUNDA_TASK_BUSINESS_RULE
import org.camunda.bpm.engine.ActivityTypes.TASK_MANUAL_TASK as CAMUNDA_TASK_MANUAL_TASK
import org.camunda.bpm.engine.ActivityTypes.TASK_RECEIVE_TASK as CAMUNDA_TASK_RECEIVE_TASK
import org.camunda.bpm.engine.ActivityTypes.TASK_SCRIPT as CAMUNDA_TASK_SCRIPT
import org.camunda.bpm.engine.ActivityTypes.TASK_SEND_TASK as CAMUNDA_TASK_SEND_TASK
import org.camunda.bpm.engine.ActivityTypes.TASK_SERVICE as CAMUNDA_TASK_SERVICE
import org.camunda.bpm.engine.ActivityTypes.TASK_USER_TASK as CAMUNDA_TASK_USER_TASK
import org.camunda.bpm.engine.ActivityTypes.TRANSACTION as CAMUNDA_TRANSACTION

enum class ActivityType(
    val bpmnModelValue: String
) {
    // Service task
    @Deprecated("Marked for removal since 10.4.0")
    SERVICE_TASK("bpmn:" + CAMUNDA_TASK_SERVICE.replaceFirstChar { it.uppercaseChar() } + ":" + ExecutionListener.EVENTNAME_START),

    @Deprecated("Marked for removal since 10.4.0")
    OLD_SERVICE_TASK("bpmn:" + CAMUNDA_TASK_SERVICE.replaceFirstChar { it.uppercaseChar() }),
    SERVICE_TASK_START("bpmn:" + CAMUNDA_TASK_SERVICE.replaceFirstChar { it.uppercaseChar() } + ":" + ExecutionListener.EVENTNAME_START),

    // User task
    @Deprecated("Marked for removal since 10.4.0")
    OLD_USER_TASK("bpmn:" + CAMUNDA_TASK_USER_TASK.replaceFirstChar { it.uppercaseChar() }),

    @Deprecated("Marked for removal since 10.4.0")
    USER_TASK("bpmn:" + CAMUNDA_TASK_USER_TASK.replaceFirstChar { it.uppercaseChar() }),
    USER_TASK_CREATE("bpmn:" + CAMUNDA_TASK_USER_TASK.replaceFirstChar { it.uppercaseChar() } + ":" + TaskListener.EVENTNAME_CREATE),


    // Unsupported
    MULTI_INSTANCE_BODY("bpmn:" + CAMUNDA_MULTI_INSTANCE_BODY.replaceFirstChar { it.uppercaseChar() }),

    EXCLUSIVE_GATEWAY("bpmn:" + CAMUNDA_GATEWAY_EXCLUSIVE.replaceFirstChar { it.uppercaseChar() }),
    INCLUSIVE_GATEWAY("bpmn:" + CAMUNDA_GATEWAY_INCLUSIVE.replaceFirstChar { it.uppercaseChar() }),
    PARALLEL_GATEWAY("bpmn:" + CAMUNDA_GATEWAY_PARALLEL.replaceFirstChar { it.uppercaseChar() }),
    COMPLEX_GATEWAY("bpmn:" + CAMUNDA_GATEWAY_COMPLEX.replaceFirstChar { it.uppercaseChar() }),
    EVENT_BASED_GATEWAY("bpmn:" + CAMUNDA_GATEWAY_EVENT_BASED.replaceFirstChar { it.uppercaseChar() }),

    TASK("bpmn:" + CAMUNDA_TASK.replaceFirstChar { it.uppercaseChar() }),
    SCRIPT_TASK("bpmn:" + CAMUNDA_TASK_SCRIPT.replaceFirstChar { it.uppercaseChar() }),
    BUSINESS_RULE_TASK("bpmn:" + CAMUNDA_TASK_BUSINESS_RULE.replaceFirstChar { it.uppercaseChar() }),
    MANUAL_TASK("bpmn:" + CAMUNDA_TASK_MANUAL_TASK.replaceFirstChar { it.uppercaseChar() }),
    SEND_TASK("bpmn:" + CAMUNDA_TASK_SEND_TASK.replaceFirstChar { it.uppercaseChar() }),
    RECEIVE_TASK("bpmn:" + CAMUNDA_TASK_RECEIVE_TASK.replaceFirstChar { it.uppercaseChar() }),

    SUB_PROCESS("bpmn:" + CAMUNDA_SUB_PROCESS.replaceFirstChar { it.uppercaseChar() }),
    AD_HOC_SUB_PROCESS("bpmn:" + CAMUNDA_SUB_PROCESS_AD_HOC.replaceFirstChar { it.uppercaseChar() }),
    CALL_ACTIVITY_START("bpmn:" + CAMUNDA_CALL_ACTIVITY.replaceFirstChar { it.uppercaseChar() } + ":" + ExecutionListener.EVENTNAME_START),
    TRANSACTION("bpmn:" + CAMUNDA_TRANSACTION.replaceFirstChar { it.uppercaseChar() }),

    BOUNDARY_TIMER("bpmn:" + CAMUNDA_BOUNDARY_TIMER.replaceFirstChar { it.uppercaseChar() }),
    BOUNDARY_MESSAGE("bpmn:" + CAMUNDA_BOUNDARY_MESSAGE.replaceFirstChar { it.uppercaseChar() }),
    BOUNDARY_SIGNAL("bpmn:" + CAMUNDA_BOUNDARY_SIGNAL.replaceFirstChar { it.uppercaseChar() }),
    COMPENSATION_BOUNDARY_CATCH("bpmn:" + CAMUNDA_BOUNDARY_COMPENSATION.replaceFirstChar { it.uppercaseChar() }),
    BOUNDARY_ERROR("bpmn:" + CAMUNDA_BOUNDARY_ERROR.replaceFirstChar { it.uppercaseChar() }),
    BOUNDARY_ESCALATION("bpmn:" + CAMUNDA_BOUNDARY_ESCALATION.replaceFirstChar { it.uppercaseChar() }),
    CANCEL_BOUNDARY_CATCH("bpmn:" + CAMUNDA_BOUNDARY_CANCEL.replaceFirstChar { it.uppercaseChar() }),
    BOUNDARY_CONDITIONAL("bpmn:" + CAMUNDA_BOUNDARY_CONDITIONAL.replaceFirstChar { it.uppercaseChar() }),

    START_EVENT("bpmn:" + CAMUNDA_START_EVENT.replaceFirstChar { it.uppercaseChar() }),
    START_TIMER_EVENT("bpmn:" + CAMUNDA_START_EVENT_TIMER.replaceFirstChar { it.uppercaseChar() }),
    MESSAGE_START_EVENT("bpmn:" + CAMUNDA_START_EVENT_MESSAGE.replaceFirstChar { it.uppercaseChar() }),
    SIGNAL_START_EVENT("bpmn:" + CAMUNDA_START_EVENT_SIGNAL.replaceFirstChar { it.uppercaseChar() }),
    ESCALATION_START_EVENT("bpmn:" + CAMUNDA_START_EVENT_ESCALATION.replaceFirstChar { it.uppercaseChar() }),
    COMPENSATION_START_EVENT("bpmn:" + CAMUNDA_START_EVENT_COMPENSATION.replaceFirstChar { it.uppercaseChar() }),
    ERROR_START_EVENT("bpmn:" + CAMUNDA_START_EVENT_ERROR.replaceFirstChar { it.uppercaseChar() }),
    CONDITIONAL_START_EVENT("bpmn:" + CAMUNDA_START_EVENT_CONDITIONAL.replaceFirstChar { it.uppercaseChar() }),

    INTERMEDIATE_CATCH_EVENT("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_CATCH.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_MESSAGE_CATCH("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_MESSAGE.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_TIMER("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_TIMER.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_LINK_CATCH("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_LINK.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_SIGNAL_CATCH("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_SIGNAL.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_CONDITIONAL("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_CONDITIONAL.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_THROW_EVENT("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_THROW.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_SIGNAL_THROW("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_SIGNAL_THROW.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_COMPENSATION_THROW_EVENT("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_COMPENSATION_THROW.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_MESSAGE_THROW_EVENT("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_MESSAGE_THROW.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_NONE_THROW_EVENT("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_NONE_THROW.replaceFirstChar { it.uppercaseChar() }),
    INTERMEDIATE_ESCALATION_THROW_EVENT("bpmn:" + CAMUNDA_INTERMEDIATE_EVENT_ESCALATION_THROW.replaceFirstChar { it.uppercaseChar() }),

    ERROR_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_ERROR.replaceFirstChar { it.uppercaseChar() }),
    CANCEL_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_CANCEL.replaceFirstChar { it.uppercaseChar() }),
    TERMINATE_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_TERMINATE.replaceFirstChar { it.uppercaseChar() }),
    MESSAGE_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_MESSAGE.replaceFirstChar { it.uppercaseChar() }),
    SIGNAL_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_SIGNAL.replaceFirstChar { it.uppercaseChar() }),
    COMPENSATION_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_COMPENSATION.replaceFirstChar { it.uppercaseChar() }),
    ESCALATION_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_ESCALATION.replaceFirstChar { it.uppercaseChar() }),
    NONE_END_EVENT("bpmn:" + CAMUNDA_END_EVENT_NONE.replaceFirstChar { it.uppercaseChar() });

    @Deprecated("Marked for removal since 10.4.0")
    fun mapOldActivityTypeToCurrent(): ActivityType {
        if (OLD_SERVICE_TASK == this || SERVICE_TASK == this) {
            return SERVICE_TASK_START
        }
        if (OLD_USER_TASK == this || USER_TASK == this) {
            return USER_TASK_CREATE
        }
        return this
    }

    companion object {
        private val mapping = values().associateBy(ActivityType::bpmnModelValue)
        fun fromValue(value: String) = mapping[value] ?: error("Can't find ActivityType with value $value")
    }
}
