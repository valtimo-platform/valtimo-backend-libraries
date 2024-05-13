package com.ritense.formviewmodel.validation

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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

    fun validateViewModel(
        viewModelLoader: ViewModelLoader<*>,
        formDefinition: FormIoFormDefinition
    ) {
        val missingProperties = getAllMissingProperties(viewModelLoader, formDefinition)
        if (missingProperties.isNotEmpty()) {
            logger.error {
                "The following properties are missing in the view model for form " +
                    "(${viewModelLoader.getFormName()}): $missingProperties"
            }
        }
    }

    fun validateSubmission(
        viewModelLoader: ViewModelLoader<*>,
        formDefinition: FormIoFormDefinition
    ) {
        formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(viewModelLoader.getFormName())?.let {
            val submissionType = it.getSubmissionType()::class
            if (!(submissionType.isData && submissionType is Submission)) {
                throw IllegalStateException("Class ${submissionType.simpleName} must be a data class and implementing Submission")
            }

            if (submissionType.simpleName == ObjectNode::class.simpleName) {
                logger.error {
                    "Submission type for form ${viewModelLoader.getFormName()} is ObjectNode. " +
                        "This is not advised. Please create a data class for the submission."
                }
                return
            }
            val submission = submissionType as Submission
            val missingProperties = getAllMissingProperties(submission, formDefinition)
            if (missingProperties.isNotEmpty()) {
                logger.error {
                    "The following properties are missing in the submission for form " +
                        "(${viewModelLoader.getFormName()}): $it"
                }
            }
        }
    }

    private fun getAllMissingProperties(
        viewModelLoader: ViewModelLoader<*>,
        formDefinition: FormIoFormDefinition
    ): List<String> {
        val viewModelType = viewModelLoader.getViewModelType()::class
        if (!(viewModelType.isData && viewModelType is ViewModel)) {
            throw IllegalStateException("Class ${viewModelType.simpleName} must be a data class and implementing ViewModel")
        }
        return extractFieldNames(viewModelLoader.getViewModelType()::class).filter { fieldName ->
            fieldName !in formDefinition.inputFields.map { objectNode ->
                objectNode["key"].asText()
            }
        }
    }

    private fun getAllMissingProperties(
        submission: Submission,
        formDefinition: FormIoFormDefinition
    ): List<String> {
        return extractFieldNames(submission::class).filter { fieldName ->
            fieldName !in formDefinition.inputFields.map { objectNode ->
                objectNode["key"].asText()
            }
        }
    }

    fun extractFieldNames(kClass: KClass<*>, prefix: String = ""): List<String> {
        val results = mutableListOf<String>()

        // Iterate over each member property of the class
        for (prop in kClass.memberProperties) {
            prop.isAccessible = true  // Make private properties accessible

            // Determine the return type of the property
            val returnType = prop.returnType.classifier as? KClass<*>
            if (returnType != null && returnType.isData) {
                // If the return type is a data class, recurse into it
                results.addAll(extractFieldNames(returnType, "$prefix${prop.name}."))
            } else {
                // Otherwise, add the property name to results
                results.add("$prefix${prop.name}")
            }
        }
        return results
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}