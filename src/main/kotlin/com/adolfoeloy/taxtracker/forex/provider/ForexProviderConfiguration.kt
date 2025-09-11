package com.adolfoeloy.taxtracker.forex.provider

import com.adolfoeloy.taxtracker.forex.ExchangeRateRepository
import com.adolfoeloy.taxtracker.properties.TaxProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ForexProviderConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "tax.currencyBeacon",
        name = ["enabled"],
        havingValue = "false"
    )
    fun localForexProvider(): ForexProvider {
        return LocalForexProvider()
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "tax.currencyBeacon",
        name = ["enabled"],
        havingValue = "true"
    )
    fun decoratedForexProvider(
        taxProperties: TaxProperties,
        restTemplate: RestTemplate,
        exchangeRateRepository: ExchangeRateRepository
    ): ForexProvider {

        val currencyBeaconProvider = CurrencyBeaconProvider(
            taxProperties = taxProperties,
            restTemplate = restTemplate
        )

        return ForexProviderDBDecorator(currencyBeaconProvider, exchangeRateRepository)
    }

}