package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

class OnStartUpViewModelValidator(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val viewModelLoaders: List<ViewModelLoader<*>>
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        validateAllViewModels()
    }

    fun validateAllViewModels() {
        for (viewModelLoader in viewModelLoaders) {
            validateViewModel(viewModelLoader)?.let {
                logger.error {
                    "The following properties are missing in the view model for form " +
                        "(${viewModelLoader.getFormName()}): $it"
                }
            }
        }
    }

    // TODO how to simulate the submission of a form definition?
    private fun validateViewModel(viewModelLoader: ViewModelLoader<*>): List<String>? {
        val form = formIoFormDefinitionService.getFormDefinitionByName(viewModelLoader.getFormName()).get()
        if (!extractProperties(viewModelLoader.getViewModelType().java).containsAll(getFormKeys(form))) {
            val missingProperties = getAllMissingProperties(viewModelLoader, form)
            return missingProperties
        } else {
            return null
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