package com.ritense.haalcentraal

abstract class BaseTest {
    fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName)!!.readText(Charsets.UTF_8)
}