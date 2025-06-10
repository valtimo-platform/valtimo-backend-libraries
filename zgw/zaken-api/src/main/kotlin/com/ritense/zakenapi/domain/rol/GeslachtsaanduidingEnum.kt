package com.ritense.zakenapi.domain.rol

/**
 * Mogelijke waarden voor geslachtsaanduiding:
 * - M: Man
 * - V: Vrouw
 * - O: Onbekend
 */
enum class GeslachtsaanduidingEnum(val value: String) {
    M("m"),
    V("v"),
    O("o")
}