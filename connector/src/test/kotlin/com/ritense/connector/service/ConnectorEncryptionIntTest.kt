package com.ritense.connector.service

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.connector.BaseIntegrationTest
import com.ritense.connector.impl.NestedObject
import com.ritense.connector.impl.ObjectApiProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.SpringHandlerInstantiator
import javax.inject.Inject

internal class ConnectorEncryptionIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var handlerInstantiator: SpringHandlerInstantiator

    @Test
    fun `should encrypt and decrypt`() {
        val mapper = JsonMapper.builder()
            .addModule(KotlinModule())
            .build()
        mapper.setHandlerInstantiator(handlerInstantiator)

        val value = mapper.writeValueAsString(ObjectApiProperties(NestedObject("cd63e158f3aca276ef284e3033d020a22899c728")))

        assertThat(value).doesNotContain("cd63e158f3aca276ef284e3033d020a22899c728")

        val decrypted: ObjectApiProperties = mapper.readValue(value)

        assertThat(decrypted).isNotNull
        assertThat(decrypted.nestedObject.name).contains("cd63e158f3aca276ef284e3033d020a22899c728")
    }
}