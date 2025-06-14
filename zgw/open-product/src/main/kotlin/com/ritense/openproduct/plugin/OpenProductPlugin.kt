/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.openproduct.plugin

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.openproduct.client.*
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.tokenauthentication.plugin.TokenAuthenticationPlugin
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.delegate.DelegateExecution

@Plugin(
    key = "openproduct",
    title = "Open Product",
    description = "Plugin for interacting with the Open Product API"
)
class OpenProductPlugin(
    pluginService: PluginService,
    private val openProductClient: OpenProductClient,
    private val valueResolverService: ValueResolverService
) {
    private val objectMapper = pluginService.getObjectMapper()

    @PluginProperty(key = "baseUrl", secret = false, required = true)
    lateinit var baseUrl: String

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: TokenAuthenticationPlugin

    @PluginAction(
        key = "create-product",
        title = "Create product plugin action",
        description = "Create product plugin action",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START],
    )
    fun createProduct(
        execution: DelegateExecution,
        @PluginActionProperty productNaam: String,
        @PluginActionProperty productTypeUUID: String,
        @PluginActionProperty eigenaarBSN: String,
        @PluginActionProperty eigenaarData: List<DataBindingConfig>?,
        @PluginActionProperty gepubliceerd: Boolean,
        @PluginActionProperty productPrijs: String,
        @PluginActionProperty frequentie: String,
        @PluginActionProperty status: String,
        @PluginActionProperty resultaatPV: String,
    ) {

        val freqenum = toFreqEnum(frequentie)
        val statusEnum = toStatusEnum(status)

        val resultaat = openProductClient.createProduct(
            baseUrl,
            authenticationPluginConfiguration,
            ProductRequest(
                naam = productNaam,
                producttypeUuid = productTypeUUID,
                eigenaren = listOf(
                    EigenaarRequest(
                        bsn = eigenaarBSN
                    )
                ),
                gepubliceerd = gepubliceerd,
                prijs = productPrijs,
                frequentie = freqenum,
                status = statusEnum,
            )
        )

        println("Resultaat van aanmaken product: $resultaat")
        execution.setVariable(resultaatPV, resultaat)
        println("Product aangemaakt met de naam: $productNaam, resultaat opgeslagen in de procesvariabele: $resultaatPV")
    }

    @PluginAction(
        key = "update-product",
        title = "Update product plugin action",
        description = "Update product plugin action",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START],
    )
    fun updateProduct(
        execution: DelegateExecution,
        @PluginActionProperty productUUID: String,
        @PluginActionProperty productNaam: String,
        @PluginActionProperty productTypeUUID: String,
        @PluginActionProperty eigenaarBSN: String,
        @PluginActionProperty gepubliceerd: Boolean,
        @PluginActionProperty productPrijs: String,
        @PluginActionProperty frequentie: String,
        @PluginActionProperty status: String,
        @PluginActionProperty resultaatPV: String
    ) {
        val requestMap = mutableMapOf<String, Any>()

        requestMap["uuid"] = productUUID
        if (productNaam.isNotBlank()) requestMap["naam"] = productNaam
        if (productTypeUUID.isNotBlank()) requestMap["producttypeUuid"] = productTypeUUID
        if (eigenaarBSN.isNotBlank()) requestMap["eigenaren"] = listOf(mapOf("bsn" to eigenaarBSN))
        requestMap["gepubliceerd"] = gepubliceerd
        if (productPrijs.isNotBlank()) requestMap["prijs"] = productPrijs
        if (frequentie.isNotBlank()) requestMap["frequentie"] = toFreqEnum(frequentie)
        if (status.isNotBlank()) requestMap["status"] = toStatusEnum(status)

        println(requestMap)

        val result = openProductClient.updateProduct(
            baseUrl,
            authenticationPluginConfiguration,
            requestMap
        )

        execution.setVariable(resultaatPV, result)
        println("Product geupdate met UUID: $productUUID, resultaat opgeslagen in de procesvariabele: $resultaatPV")
    }

    @PluginAction(
        key = "delete-product",
        title = "Delete product plugin action",
        description = "Delete product plugin action",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START],
    )
    fun deleteProduct(
        execution: DelegateExecution,
        @PluginActionProperty productUUID: String,
        @PluginActionProperty resultaatPV: String
    ) {
        val result = openProductClient.deleteProduct(
            baseUrl,
            authenticationPluginConfiguration,
            productUUID
        )

        execution.setVariable(resultaatPV, "Product verwijderd met UUID: $productUUID")
        println("Product verwijderd met UUID: $productUUID, resultaat opgeslagen in procesvariabele: $resultaatPV")
    }

    fun toFreqEnum(frequentie: String): FrequentieEnum {
        return when (frequentie) {
            "eenmalig" -> FrequentieEnum.EENMALIG
            "jaarlijks" -> FrequentieEnum.JAARLIJKS
            "maandelijks" -> FrequentieEnum.MAANDELIJKS
            else -> throw IllegalArgumentException("Ongeldige frequentie: $frequentie")
        }
    }

    fun toStatusEnum(status: String): StatusEnum {
        return when (status) {
            "initieel" -> StatusEnum.INITIEEL
            "gereed" -> StatusEnum.GEREED
            "actief" -> StatusEnum.ACTIEF
            "ingetrokken" -> StatusEnum.INGETROKKEN
            "geweigerd" -> StatusEnum.GEWEIGERD
            "verlopen" -> StatusEnum.VERLOPEN
            else -> throw IllegalArgumentException("Ongeldige status: $status")
        }
    }

    private fun getEigenaarData(
        keyValueMap: List<DataBindingConfig>,
        documentId: String
    ): JsonNode {
        val resolvedValuesMap = valueResolverService.resolveValues(
            documentId, keyValueMap.map { it.value }
        )

        if (keyValueMap.size != resolvedValuesMap.size) {
            val failedValues = keyValueMap
                .filter { !resolvedValuesMap.containsKey(it.value) }
                .joinToString(", ") { "'${it.key}' = '${it.value}'" }
            throw IllegalArgumentException(
                "Error in case: '${documentId}'. Failed to resolve values: $failedValues".trimMargin()
            )
        }

        val objectDataMap = keyValueMap.associate { it.key to resolvedValuesMap[it.value] }

        val objectData = objectMapper.createObjectNode()
        val jsonPatchBuilder = JsonPatchBuilder()

        objectDataMap.forEach {
            val path = JsonPointer.valueOf(it.key)
            val valueNode = objectMapper.valueToTree<JsonNode>(it.value)
            jsonPatchBuilder.addJsonNodeValue(objectData, path, valueNode)
        }

        JsonPatchService.apply(jsonPatchBuilder.build(), objectData)

        return objectMapper.convertValue(objectData)
    }

}
