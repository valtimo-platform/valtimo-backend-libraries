package com.ritense.openproduct.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductRequest(
    @JsonProperty("naam")
    val naam: String? = null,
    @JsonProperty("start_datum")
    val startDatum: LocalDate? = null,
    @JsonProperty("eind_datum")
    val eindDatum: LocalDate? = null,
    @JsonProperty("producttype_uuid")
    val producttypeUuid: String,
    @JsonProperty("gepubliceerd")
    val gepubliceerd: Boolean? = null,
    @JsonProperty("eigenaren")
    val eigenaren: List<EigenaarRequest>,
    @JsonProperty("documenten")
    val documenten: List<DocumentRequest>? = null,
    @JsonProperty("status")
    val status: StatusEnum? = null,
    @JsonProperty("prijs")
    val prijs: String,
    @JsonProperty("frequentie")
    val frequentie: FrequentieEnum,
    @JsonProperty("verbruiksobject")
    val verbruiksobject: Map<String, Any>? = null,
    @JsonProperty("dataobject")
    val dataobject: Map<String, Any>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EigenaarRequest(
    @JsonProperty("uuid")
    val uuid: UUID? = null,
    @JsonProperty("bsn")
    val bsn: String? = null,
    @JsonProperty("kvk_nummer")
    val kvkNummer: String? = null,
    @JsonProperty("vestigingsnummer")
    val vestigingsnummer: String? = null,
    @JsonProperty("klantnummer")
    val klantnummer: String? = null
)

data class DocumentRequest(
    @JsonProperty("uuid")
    val uuid: UUID
)


data class Product(
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
    @JsonProperty("eigenaren")
    val eigenaren: List<Eigenaar>,
    @JsonProperty("documenten")
    val documenten: List<Document>,
    @JsonProperty("status")
    val status: StatusEnum,
    @JsonProperty("prijs")
    val prijs: String,
    @JsonProperty("frequentie")
    val frequentie: FrequentieEnum,
    @JsonProperty("verbruiksobject")
    val verbruiksobject: Map<String, Any>?,
    @JsonProperty("dataobject")
    val dataobject: Map<String, Any>?
)

data class ProductType(
    @JsonProperty("uuid")
    val uuid: UUID
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Eigenaar(
    @JsonProperty("uuid")
    val uuid: UUID,
    @JsonProperty("bsn")
    val bsn: String?,
    @JsonProperty("kvk_nummer")
    val kvkNummer: String?,
    @JsonProperty("vestigingsnummer")
    val vestigingsnummer: String?,
    @JsonProperty("klantnummer")
    val klantnummer: String?
)

data class Document(
    @JsonProperty("url")
    val url: String
)
