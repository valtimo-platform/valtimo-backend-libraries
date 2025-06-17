package com.ritense.openproduct.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.util.UUID


data class PaginatedProductList(
    val aantal: Int,
    val volgende: String?,
    val vorige: String?,
    val resultaten: List<ProductResponse>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductResponse(
    @JsonProperty("uuid")
    val uuid: UUID,
    @JsonProperty("url")
    val url: String,
    @JsonProperty("naam")
    val naam: String?,
    @JsonProperty("start_datum")
    val startDatum: LocalDate?,
    @JsonProperty("eind_datum")
    val eindDatum: LocalDate?,
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: String,
    @JsonProperty("update_datum")
    val updateDatum: String,
    @JsonProperty("producttype")
    val producttype: ProductType,
    @JsonProperty("gepubliceerd")
    val gepubliceerd: Boolean,
    @JsonProperty("status")
    val status: StatusEnum,
    @JsonProperty("prijs")
    val prijs: String,
)