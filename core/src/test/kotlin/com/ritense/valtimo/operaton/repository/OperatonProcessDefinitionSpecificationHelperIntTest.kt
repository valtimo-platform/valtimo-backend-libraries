package com.ritense.valtimo.operaton.repository

import com.ritense.valtimo.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.operaton.bpm.engine.RepositoryService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class OperatonProcessDefinitionSpecificationHelperIntTest @Autowired constructor(
    private val definitionRepository: OperatonProcessDefinitionRepository,
    private val repositoryService: RepositoryService
) : BaseIntegrationTest() {

    @Test
    @Transactional
    fun byId() {
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(USER_TASK_PROCESS)
            .latestVersion()
            .singleResult()

        val result = definitionRepository.findOne(
                OperatonProcessDefinitionSpecificationHelper.byId(processDefinition.id)
        ).get()
        Assertions.assertThat(result.key).isEqualTo(USER_TASK_PROCESS)
    }

    @Test
    @Transactional
    fun byKey() {
        val processDefinitionIds = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(USER_TASK_PROCESS)
            .list()
            .map { it.id }

        val result = definitionRepository.findAll(
            OperatonProcessDefinitionSpecificationHelper.byKey(USER_TASK_PROCESS)
        ).map { it.id }

        Assertions.assertThat(result).containsAll(processDefinitionIds)
    }

    @Test
    @Transactional
    fun byVersion() {
        val deployedProcessDefinition = repositoryService.createDeployment()
            .addClasspathResource("config/case/everything/1-0-0/bpmn/$USER_TASK_PROCESS.bpmn")
            .deployWithResult()
            .deployedProcessDefinitions.first()

        val version1Id = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(USER_TASK_PROCESS)
            .processDefinitionVersion(1)
            .singleResult().id

        val resultIds = definitionRepository.findAll(
            OperatonProcessDefinitionSpecificationHelper.byKey(USER_TASK_PROCESS)
                .and(OperatonProcessDefinitionSpecificationHelper.byVersion(2))
        ).map { it.id }

        Assertions.assertThat(resultIds).contains(deployedProcessDefinition.id)
        Assertions.assertThat(resultIds).doesNotContain(version1Id)
    }

    @Test
    @Transactional
    fun byLatestVersion() {
        val deployedProcessDefinition = repositoryService.createDeployment()
            .addClasspathResource("config/case/everything/1-0-0/bpmn/$USER_TASK_PROCESS.bpmn")
            .deployWithResult()
            .deployedProcessDefinitions.first()

        val version1Id = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(USER_TASK_PROCESS)
            .processDefinitionVersion(1)
            .singleResult().id

        val resultIds = definitionRepository.findAll(
            OperatonProcessDefinitionSpecificationHelper.byKey(USER_TASK_PROCESS)
                .and(OperatonProcessDefinitionSpecificationHelper.byLatestVersion())
        ).map { it.id }

        Assertions.assertThat(resultIds).contains(deployedProcessDefinition.id)
        Assertions.assertThat(resultIds).doesNotContain(version1Id)
    }

    @Test
    @Transactional
    fun byActive() {
        val deployedProcessDefinition = repositoryService.createDeployment()
            .addClasspathResource("config/case/everything/1-0-0/bpmn/$USER_TASK_PROCESS.bpmn")
            .deployWithResult()
            .deployedProcessDefinitions.first()

        val version1Id = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(USER_TASK_PROCESS)
            .processDefinitionVersion(1)
            .singleResult().id

        repositoryService.suspendProcessDefinitionById(version1Id)

        val resultIds = definitionRepository.findAll(
            OperatonProcessDefinitionSpecificationHelper.byKey(USER_TASK_PROCESS)
                .and(OperatonProcessDefinitionSpecificationHelper.byActive())
        ).map { it.id }

        Assertions.assertThat(resultIds).contains(deployedProcessDefinition.id)
        Assertions.assertThat(resultIds).doesNotContain(version1Id)
    }

    companion object {
        const val USER_TASK_PROCESS = "user-task-process"
    }
}