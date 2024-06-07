package com.ritense.formviewmodel.validation

import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.viewmodel.Submission
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DataClassPropertiesExtractorTest : BaseTest() {
    // Example ViewModels
    data class Person(val name: String, val address: Address)
    data class Address(val street: String, val city: City)
    data class City(val name: String, val code: Int)

    @Test
    fun `should extract all ViewModel field names`() {
        DataClassPropertiesExtractor.extractProperties(Person::class).let {
            assertTrue(it.contains("address.city.code"))
            assertTrue(it.contains("address.city.name"))
            assertTrue(it.contains("address.street"))
            assertTrue(it.contains("name"))
        }
    }

    data class MySubmission(val name: String, val address: Address) : Submission

    @Test
    fun `should extract all field names for Submission`() {
        DataClassPropertiesExtractor.extractProperties(MySubmission::class).let {
            assertTrue(it.contains("name"))
            assertTrue(it.contains("address.city.code"))
            assertTrue(it.contains("address.city.name"))
        }
    }
}
