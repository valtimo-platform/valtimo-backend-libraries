package com.inwonerplan.poc

import com.inwonerplan.poc.aanbod.AanbodViewModelLoader
import com.inwonerplan.poc.aanbod.OnAanbodSubmittedEventHandler
import com.inwonerplan.poc.aanbod.command.SaveAanbodSubmissionCommandHandler
import com.inwonerplan.poc.aandachtspunten.OnAandachtsPuntenSubmittedEventHandler
import com.inwonerplan.poc.aandachtspunten.AandachtsPuntenViewModelLoader
import com.inwonerplan.poc.subdoelen.OnSubdoelenSubmittedEventHandler
import com.inwonerplan.poc.subdoelen.SubdoelenViewModelLoader
import com.ritense.document.service.DocumentService
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandDispatcher
import com.ritense.formviewmodel.commandhandling.CommandHandler
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class PocAutoConfiguration {

    @Bean
    fun aandachtsPuntenViewModelLoader() = AandachtsPuntenViewModelLoader()
    @Bean
    fun onAandachtsPuntenSubmittedEventHandler() = OnAandachtsPuntenSubmittedEventHandler()
    @Bean
    fun subdoelenViewModelLoader() = SubdoelenViewModelLoader()
    @Bean
    fun onSubdoelenSubmittedEventHandler() = OnSubdoelenSubmittedEventHandler()
    @Bean
    fun aanbodViewModelLoader() = AanbodViewModelLoader()
    @Bean
    fun onAanbodSubmittedEventHandler() : FormViewModelSubmissionHandler<*> = OnAanbodSubmittedEventHandler()

    @Bean
    fun saveAanbodSubmissionCommandHandler(
        documentService: DocumentService
    ) = SaveAanbodSubmissionCommandHandler(
        documentService
    )

}