package autoconfigure

import com.ritense.iko.service.ConfigurationService
import com.ritense.iko.service.IkoValueResolver
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class IkoAutoConfiguration {

    @Bean
    fun configurationService() = ConfigurationService()

    @Bean
    fun ikoValueResolver() = IkoValueResolver()

}