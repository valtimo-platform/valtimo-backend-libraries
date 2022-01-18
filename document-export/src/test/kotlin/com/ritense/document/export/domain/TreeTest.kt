package com.ritense.document.export.domain

import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.export.BaseTest
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TreeTest : BaseTest() {

    @Test
    fun `should initialize tree`() {
        val jsonSchema = JsonSchema.fromResourceUri(path("testschema"))

        val tree = Tree.init(jsonSchema.asJson())

        assertThat(tree).isNotNull
        assertThat(tree.root).isNotEmpty
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}