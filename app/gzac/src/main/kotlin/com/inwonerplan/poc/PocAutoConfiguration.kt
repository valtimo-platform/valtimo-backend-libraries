package com.inwonerplan.poc

import com.inwonerplan.poc.aanbod.AanbodViewModelLoader
import com.inwonerplan.poc.aandachtspunten.OnAandachtsPuntenSubmittedEventHandler
import com.inwonerplan.poc.aandachtspunten.AandachtsPuntenViewModelLoader
import com.inwonerplan.poc.subdoelen.OnSubdoelenSubmittedEventHandler
import com.inwonerplan.poc.subdoelen.SubdoelenViewModelLoader
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

}