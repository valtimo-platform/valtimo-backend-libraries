package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class AardRelatie(@JsonValue val key: String) {
    BIJDRAGE("bijdrage"),
    ONDERWERP("onderwerp"),
    OVERIG("overig"),
    VERVOLG("vervolg"),
}