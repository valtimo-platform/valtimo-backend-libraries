package autoconfigure

import com.ritense.iko.service.ConfigurationService
import com.ritense.iko.service.FieldsIkoWidgetDataProvider
import com.ritense.iko.service.IkoValueResolver
import com.ritense.iko.service.IkoValueResolverService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@AutoConfiguration
class IkoAutoConfiguration {

    @Bean
    fun configurationService() = ConfigurationService()

    @Bean
    fun ikoValueResolver(restClientBuilder: RestClient.Builder) = IkoValueResolver(restClientBuilder)

    @Bean
    fun ikoValueResolverService(ikoValueResolver: IkoValueResolver) = IkoValueResolverService(ikoValueResolver)

    @Bean
    fun fieldsIkoWidgetDataProvider(
        ikoValueResolverService: IkoValueResolverService
    ) = FieldsIkoWidgetDataProvider(
        ikoValueResolverService
    )

}