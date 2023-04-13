package com.ritense.externalevent.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(MappedCasesConfig::class)
@TestPropertySource("classpath:mapping-config.properties")
internal class BindingPropertiesToMappedCasesConfigTest {

    @Autowired
    private val mappedCasesConfig: MappedCasesConfig? = null

    @Test
    fun shouldBindMappingConfig() {
        assertThat(mappedCasesConfig!!.links).isNotNull

        assertThat(mappedCasesConfig.links)
            .containsEntry("person", PortalMapping("a", "b"))

        assertThat(mappedCasesConfig.links)
            .containsEntry("test", PortalMapping("c", "d"))
    }

}
