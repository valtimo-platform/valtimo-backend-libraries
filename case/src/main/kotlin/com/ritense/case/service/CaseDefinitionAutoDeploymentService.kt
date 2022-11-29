package com.ritense.case.service

import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

open class CaseDefinitionAutoDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val deploymentService: CaseDefinitionDeploymentService
) {

    @Transactional
    @EventListener(ApplicationStartedEvent::class)
    open fun deployCaseDefinitions() {
        logger.info { "Deploying case definitions" }
        loadCaseDefinitionResources().forEach { resource ->
            if (resource.filename != null) {
                deploymentService.deploy(
                    resource.filename!!.substringBeforeLast("."),
                    StreamUtils.copyToString(resource.inputStream, StandardCharsets.UTF_8)
                )
            }
        }
    }

    private fun loadCaseDefinitionResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(CASE_DEFINITIONS_PATH)
    }

    companion object {
        private const val CASE_DEFINITIONS_PATH = "classpath:config/case/definition/*.json"
        val logger = KotlinLogging.logger {}
    }
}