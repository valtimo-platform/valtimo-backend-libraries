/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.mail.service

import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import java.util.Optional
import java.util.regex.Pattern

class MailService(
    private val mailSender: MailSender
) {

    fun sendElementTemplateTaskMail(
        delegateExecution: DelegateExecution
    ): Optional<List<MailMessageStatus>>? {
        val mailSettings = getMailSettings(delegateExecution)
        return mailSender.send(mailSettings.getTemplatedMailMessage())
    }

    fun getMailSettings(delegateExecution: DelegateExecution): MailSettings {
        var camundaPropertiesMap = mutableMapOf<String, Any>()
        camundaPropertiesMap = delegateExecution
            .bpmnModelElementInstance
            .extensionElements
            .elementsQuery
            .filterByType(CamundaProperties::class.java)
            .singleResult()
            .camundaProperties
            .associateTo(camundaPropertiesMap) {
                it.getAttributeValue("name") to parseValue(it.camundaValue, delegateExecution)
            }
        return MailSettings(camundaPropertiesMap, delegateExecution)
    }

    private fun parseValue(value: String, delegateExecution: DelegateExecution): String {
        val camundaExpressionMatcher = MailSettings.camundaExpressionPattern.matcher(value)
        return if (camundaExpressionMatcher.find()) {
            val keyNameFromExpression = camundaExpressionMatcher.group(1)
            // return key value from process variables
            // for example '${emailaddress}' will result in 'emailaddress'
            delegateExecution.variables[keyNameFromExpression] as String
        } else {
            value
        }
    }

    data class MailSettings(
        val map: Map<String, Any?>,
        val delegateExecution: DelegateExecution
    ) {
        val mailSendTaskTo: String by map
        val mailSendTaskFrom: String by map
        val mailSendTaskSubject: String by map
        val mailSendTaskTemplate: String by map

        fun getRecipient(): Recipient {
            return Recipient.to(EmailAddress.from(mailSendTaskTo), SimpleName.none())
        }

        fun getSender(): Sender {
            return Sender.from(EmailAddress.from(mailSendTaskFrom))
        }

        fun getSubject(): Subject {
            return Subject.from(mailSendTaskSubject)
        }

        fun getPlaceholders(): Map<String, Any> {
            return mapOf<String, Any>(
                "business-key" to delegateExecution.processBusinessKey,
                "var" to delegateExecution.variables
            )
        }

        fun getTemplatedMailMessage(): TemplatedMailMessage {
            return TemplatedMailMessage.with(getRecipient(), MailTemplateIdentifier.from(mailSendTaskTemplate))
                .placeholders(getPlaceholders())
                .subject(getSubject())
                .sender(getSender())
                .build()
        }

        companion object {
            // Check for `${someProcessVarName}` pattern
            val camundaExpressionPattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_\\-\\.]+)\\}$")!!
        }
    }

}