package com.ritense.haalcentraal.brp

abstract class BaseTest {
    fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName)!!.readText(Charsets.UTF_8)
}