package com.ritense.formviewmodel.validation

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

class OnStartUpViewModelValidator(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val viewModelLoaders: List<ViewModelLoader<*>>,
    private val formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        validateAllViewModels()
    }

    fun validateAllViewModels() {
        for (viewModelLoader in viewModelLoaders) {
            val form = formIoFormDefinitionService.getFormDefinitionByName(viewModelLoader.getFormName()).get()
            validateViewModel(viewModelLoader, form)
            validateSubmission(viewModelLoader, form)
        }
    }

    private fun validateSubmission(viewModelLoader: ViewModelLoader<*>, form: FormIoFormDefinition) {
        formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(viewModelLoader.getFormName())?.let {
            val submissionType = it.getSubmissionType().java
            if (submissionType.simpleName == ObjectNode::class.simpleName) {
                return
            }
            submissionType.declaredFields.map { it.name }.filter {
                it !in getFormKeys(form)
            }.let {
                if (it.isNotEmpty()) {
                    logger.error {
                        "The following properties are missing in the submission for form " +
                            "(${viewModelLoader.getFormName()}): $it"
                    }
                }
            }
        }
    }

    private fun validateViewModel(viewModelLoader: ViewModelLoader<*>, form: FormIoFormDefinition) {
        if (!extractProperties(viewModelLoader.getViewModelType().java).containsAll(getFormKeys(form))) {
            val missingProperties = getAllMissingProperties(viewModelLoader, form)
            logger.error {
                "The following properties are missing in the view model for form " +
                    "(${viewModelLoader.getFormName()}): $missingProperties"
            }
        }
    }

    private fun getAllMissingProperties(viewModelLoader: ViewModelLoader<*>, form: FormIoFormDefinition): List<String> {
        return extractProperties(viewModelLoader.getViewModelType().java).filter {
            it !in form.inputFields.map {
                it["key"].asText()
            }
        }
    }

    private fun getFormKeys(form: FormIoFormDefinition): List<String> {
        return form.inputFields.map {
            it["key"].asText()
        }
    }

    private fun extractProperties(clazz: Class<*>): List<String> {
        return clazz.declaredFields.map { it.name }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}