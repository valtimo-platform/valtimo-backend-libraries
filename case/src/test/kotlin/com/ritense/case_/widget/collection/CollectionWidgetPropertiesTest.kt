package com.ritense.case_.widget.collection

import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollectionWidgetPropertiesTest {

    @Test
    fun `field width should serialize as lowercase`() {
        val width = CollectionWidgetProperties.FieldWidth.FULL

        assertThat(width.name).isEqualTo("FULL")
        assertThat(width.value).isEqualTo(width.name.lowercase())

        assertThat(MapperSingleton.get().writeValueAsString(width)).isEqualTo("\"${width.value}\"")
    }

    @Test
    fun `field width should deserialize from lowercase`() {
        val width: CollectionWidgetProperties.FieldWidth = MapperSingleton.get().readValue("\"full\"")

        assertThat(width).isEqualTo(CollectionWidgetProperties.FieldWidth.FULL)
    }
}