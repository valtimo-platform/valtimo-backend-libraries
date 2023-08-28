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

package com.ritense.processlink.domain

import com.fasterxml.jackson.annotation.JsonValue
import org.camunda.bpm.engine.delegate.ExecutionListener.EVENTNAME_START
import org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE
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

enum class ActivityTypeWithEventName(
    @JsonValue val value: String
) {
    MULTI_INSTANCE_BODY(getActivityTypeWithEventNameValue(CAMUNDA_MULTI_INSTANCE_BODY, EVENTNAME_START)),

    EXCLUSIVE_GATEWAY(getActivityTypeWithEventNameValue(CAMUNDA_GATEWAY_EXCLUSIVE, EVENTNAME_START)),
    INCLUSIVE_GATEWAY(getActivityTypeWithEventNameValue(CAMUNDA_GATEWAY_INCLUSIVE, EVENTNAME_START)),
    PARALLEL_GATEWAY(getActivityTypeWithEventNameValue(CAMUNDA_GATEWAY_PARALLEL, EVENTNAME_START)),
    COMPLEX_GATEWAY(getActivityTypeWithEventNameValue(CAMUNDA_GATEWAY_COMPLEX, EVENTNAME_START)),
    EVENT_BASED_GATEWAY(getActivityTypeWithEventNameValue(CAMUNDA_GATEWAY_EVENT_BASED, EVENTNAME_START)),

    TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK, EVENTNAME_START)),
    SCRIPT_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_SCRIPT, EVENTNAME_START)),
    SERVICE_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_SERVICE, EVENTNAME_START)),
    BUSINESS_RULE_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_BUSINESS_RULE, EVENTNAME_START)),
    MANUAL_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_MANUAL_TASK, EVENTNAME_START)),
    USER_TASK_CREATE(getActivityTypeWithEventNameValue(CAMUNDA_TASK_USER_TASK, EVENTNAME_CREATE)),
    USER_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_USER_TASK, EVENTNAME_START)),
    SEND_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_SEND_TASK, EVENTNAME_START)),
    RECEIVE_TASK_START(getActivityTypeWithEventNameValue(CAMUNDA_TASK_RECEIVE_TASK, EVENTNAME_START)),

    SUB_PROCESS_START(getActivityTypeWithEventNameValue(CAMUNDA_SUB_PROCESS, EVENTNAME_START)),
    AD_HOC_SUB_PROCESS_START(getActivityTypeWithEventNameValue(CAMUNDA_SUB_PROCESS_AD_HOC, EVENTNAME_START)),
    CALL_ACTIVITY_START(getActivityTypeWithEventNameValue(CAMUNDA_CALL_ACTIVITY, EVENTNAME_START)),
    TRANSACTION_START(getActivityTypeWithEventNameValue(CAMUNDA_TRANSACTION, EVENTNAME_START)),

    BOUNDARY_TIMER_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_TIMER, EVENTNAME_START)),
    BOUNDARY_MESSAGE_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_MESSAGE, EVENTNAME_START)),
    BOUNDARY_SIGNAL_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_SIGNAL, EVENTNAME_START)),
    COMPENSATION_BOUNDARY_CATCH_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_COMPENSATION, EVENTNAME_START)),
    BOUNDARY_ERROR_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_ERROR, EVENTNAME_START)),
    BOUNDARY_ESCALATION_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_ESCALATION, EVENTNAME_START)),
    CANCEL_BOUNDARY_CATCH_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_CANCEL, EVENTNAME_START)),
    BOUNDARY_CONDITIONAL_START(getActivityTypeWithEventNameValue(CAMUNDA_BOUNDARY_CONDITIONAL, EVENTNAME_START)),

    START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT, EVENTNAME_START)),
    START_TIMER_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_TIMER, EVENTNAME_START)),
    MESSAGE_START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_MESSAGE, EVENTNAME_START)),
    SIGNAL_START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_SIGNAL, EVENTNAME_START)),
    ESCALATION_START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_ESCALATION, EVENTNAME_START)),
    COMPENSATION_START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_COMPENSATION, EVENTNAME_START)),
    ERROR_START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_ERROR, EVENTNAME_START)),
    CONDITIONAL_START_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_START_EVENT_CONDITIONAL, EVENTNAME_START)),

    INTERMEDIATE_CATCH_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_CATCH, EVENTNAME_START)),
    INTERMEDIATE_MESSAGE_CATCH_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_MESSAGE, EVENTNAME_START)),
    INTERMEDIATE_TIMER_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_TIMER, EVENTNAME_START)),
    INTERMEDIATE_LINK_CATCH_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_LINK, EVENTNAME_START)),
    INTERMEDIATE_SIGNAL_CATCH_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_SIGNAL, EVENTNAME_START)),
    INTERMEDIATE_CONDITIONAL_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_CONDITIONAL, EVENTNAME_START)),
    INTERMEDIATE_THROW_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_THROW, EVENTNAME_START)),
    INTERMEDIATE_SIGNAL_THROW_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_SIGNAL_THROW, EVENTNAME_START)),
    INTERMEDIATE_COMPENSATION_THROW_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_COMPENSATION_THROW, EVENTNAME_START)),
    INTERMEDIATE_MESSAGE_THROW_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_MESSAGE_THROW, EVENTNAME_START)),
    INTERMEDIATE_NONE_THROW_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_NONE_THROW, EVENTNAME_START)),
    INTERMEDIATE_ESCALATION_THROW_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_INTERMEDIATE_EVENT_ESCALATION_THROW, EVENTNAME_START)),

    ERROR_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_ERROR, EVENTNAME_START)),
    CANCEL_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_CANCEL, EVENTNAME_START)),
    TERMINATE_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_TERMINATE, EVENTNAME_START)),
    MESSAGE_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_MESSAGE, EVENTNAME_START)),
    SIGNAL_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_SIGNAL, EVENTNAME_START)),
    COMPENSATION_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_COMPENSATION, EVENTNAME_START)),
    ESCALATION_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_ESCALATION, EVENTNAME_START)),
    NONE_END_EVENT_START(getActivityTypeWithEventNameValue(CAMUNDA_END_EVENT_NONE, EVENTNAME_START));

    companion object {
        private val mapping = values().associateBy(ActivityTypeWithEventName::value)
        fun fromValue(value: String) = mapping[value] ?: error("Can't find ActivityTypeWithEventName with value $value")

    }
}

private fun getActivityTypeWithEventNameValue(activityType: String, eventName: String): String {
    return "bpmn:" + activityType.replaceFirstChar { it.uppercaseChar() } + ":" + eventName
}
