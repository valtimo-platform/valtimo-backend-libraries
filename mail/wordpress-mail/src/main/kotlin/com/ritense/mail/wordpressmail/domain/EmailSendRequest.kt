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

package com.ritense.mail.wordpressmail.domain

import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.Subject
import kotlin.streams.toList
import mu.KotlinLogging

data class EmailSendRequest(
    val variables: Map<String, Any>?,
    val to: String
) {

    companion object {
        fun from(templatedMailMessage: TemplatedMailMessage): List<EmailSendRequest> {
            if (templatedMailMessage.recipients.get().isEmpty()) {
                log.warn("No recipient found for email: ${templatedMailMessage.templateIdentifier}")
            }

            val variables = toVariablesField(templatedMailMessage.placeholders, templatedMailMessage.subject)
            return templatedMailMessage.recipients.filterTo().stream()
                .map { EmailSendRequest(variables, toToField(it)) }
                .toList()
        }

        private fun toToField(recipient: Recipient): String {
            return "${recipient.name.get()} <${recipient.email.get()}>"
        }

        private fun toVariablesField(placeholders: Map<String, Any>?, subject: Subject): Map<String, Any> {
            val variables = toVariablesField("", placeholders).associateTo(mutableMapOf()) { it.first to it.second }
            if (subject.isPresent) {
                variables["SUBJECT"] = subject.get()
            }
            return variables
        }

        private fun toVariablesField(prefix: String, placeholders: Map<String, Any>?): List<Pair<String, Any>> {
            val entries = mutableListOf<Pair<String, Any>>()
            placeholders?.entries?.forEach {
                val key = "$prefix${toVariableKey(it.key)}"
                if (it.value is Map<*, *>) {
                    entries.addAll(toVariablesField("${key}_", it.value as Map<String, Any>))
                } else {
                    entries.add(Pair(key, it.value))
                }
            }
            return entries
        }

        /* Wordpress Mail placeholder-key can only handle uppercase letters and underscore. So, no lower case and no numbers */
        private fun toVariableKey(placeholderKey: String): String {
            val snakeCase = placeholderKey.replace(Regex("([a-z])([A-Z]+)"), "\$1_\$2")
            val upperCase = snakeCase.uppercase()
            val noInvalidChars = upperCase.replace(Regex("[^A-Z_]"), "_")
            return noInvalidChars
        }

        private val log = KotlinLogging.logger {}
    }
}
