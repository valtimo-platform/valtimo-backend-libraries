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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.Document
import com.ritense.template.domain.ValtimoTemplate
import com.ritense.template.repository.TemplateRepository
import freemarker.template.Configuration
import freemarker.template.Configuration.VERSION_2_3_32
import freemarker.template.Template
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter
import java.util.UUID
import com.fasterxml.jackson.module.kotlin.convertValue
import freemarker.core.InvalidReferenceException

@Transactional
class TemplateService(
    private val templateRepository: TemplateRepository,
    private val objectMapper: ObjectMapper,
) {

    fun generate(templateKey: String, document: Document, variables: Map<String, Any>? = null): String {
        val dataModel = mutableMapOf<String, Any?>(
            "doc" to objectMapper.convertValue<Map<String, Any?>>(document.content().asJson()),
            "pv" to variables
        )
        return generate(templateKey, dataModel)
    }

    fun generate(templateKey: String, dataModel: MutableMap<String, Any?>): String {
        val valtimoTemplate = getTemplate(templateKey)
        val writer = StringWriter()
        val template = Template(UUID.randomUUID().toString(), valtimoTemplate.content, Configuration(VERSION_2_3_32))
        var exceptionCaught: Boolean
        var i = 0

        do {
            exceptionCaught = false
            i++
            try {
                template.process(dataModel, writer)
            } catch (e: InvalidReferenceException) {
                var dm = dataModel
                val parts = e.blamedExpressionString.split('.')
                parts.forEach { part ->
                    if (part == parts.last()) {
                        dm[part] = "-"
                    } else {
                        val n = (dm[part] as Map<String, Any?>?)?.toMutableMap() ?: mutableMapOf()
                        dm[part] = n
                        dm = n
                    }
                }
                exceptionCaught = true
                if (i >= 100) {
                    throw e
                }
            }
        } while (exceptionCaught)

        return writer.toString()
    }

    fun getAllTemplates(): List<ValtimoTemplate> {
        return templateRepository.findAll()
    }

    fun getTemplate(templateKey: String): ValtimoTemplate {
        return templateRepository.findById(templateKey).orElseThrow()
    }

    fun updateTemplate(key: String, content: String): ValtimoTemplate {
        return saveTemplate(
            ValtimoTemplate(
                key = key,
                content = content,
            )
        )
    }

    fun createTemplate(template: ValtimoTemplate): ValtimoTemplate {
        assert(!templateRepository.existsById(template.key)) { "Template with key '${template.key}' already exists" }
        return templateRepository.save(template)
    }

    fun saveTemplate(template: ValtimoTemplate): ValtimoTemplate {
        return templateRepository.save(template)
    }

    fun deleteTemplates(templateKeys: List<String>) {
        templateKeys.forEach { deleteTemplate(it) }
    }

    fun deleteTemplate(templateKey: String) {
        templateRepository.deleteById(templateKey)
    }
}
