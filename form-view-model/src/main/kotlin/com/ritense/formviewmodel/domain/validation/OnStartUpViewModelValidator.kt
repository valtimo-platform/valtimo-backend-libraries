package com.ritense.formviewmodel.domain.validation

import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.formviewmodel.domain.ViewModelLoader
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class OnStartUpViewModelValidator(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val context: ApplicationContext,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        validateAllViewModels()
    }

    fun validateAllViewModels() {
        for (viewModelLoader in getAllViewModelLoaders()) {
            if (!validateViewModel(viewModelLoader)) {
                throw IllegalArgumentException("ViewModelLoader ${viewModelLoader.javaClass.simpleName} is not valid for form ${viewModelLoader.getFormName()}!")
            }
        }
    }

    fun validateViewModel(viewModelLoader: ViewModelLoader<ViewModel>) : Boolean {
        return ClassPropertyExtractor().extractProperties(viewModelLoader.getViewModelType().java).containsAll(
            FormDefinitionPrefixedKeysExtractor().getPrefixedKeys(
                formIoFormDefinitionService.getFormDefinitionByName(
                    viewModelLoader.getFormName()
                ).get()
            )
        )
    }

    fun getAllViewModelLoaders(): List<ViewModelLoader<ViewModel>> {
        return context.getBeansOfType(ViewModelLoader::class.java)
            .values
            .map { it as ViewModelLoader<ViewModel> }
    }
}