package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class OnStartUpViewModelValidator(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    val viewModelLoaders: List<ViewModelLoader<*>>,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        validateAllViewModels()
    }

    fun validateAllViewModels() {
        for (viewModelLoader in viewModelLoaders) {
            val form = validateViewModel(viewModelLoader)
            if (form != null) {
                try {
                    val missingProperties = getAllMissingProperties(viewModelLoader, form)
                    throw IllegalArgumentException("The following properties are missing in the view model for form (${viewModelLoader.getFormName()}): $missingProperties")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun validateViewModel(viewModelLoader: ViewModelLoader<*>): FormIoFormDefinition? {
        val form = formIoFormDefinitionService.getFormDefinitionByName(viewModelLoader.getFormName()).get()

        if (!extractProperties(viewModelLoader.getViewModelType().java).containsAll(getFormKeys(form))) {
            return form
        }

        return null
    }

    fun getAllMissingProperties(viewModelLoader: ViewModelLoader<*>, form: FormIoFormDefinition): List<String> {
        return extractProperties(viewModelLoader.getViewModelType().java).filter {
            it !in form.inputFields.map {
                it["key"].asText()
            }
        }
    }

    fun getFormKeys(form: FormIoFormDefinition): List<String> {
        return form.inputFields.map {
            it["key"].asText()
        }
    }

    fun extractProperties(clazz: Class<*>): List<String> {
        return clazz.declaredFields.map { it.name }
    }
}