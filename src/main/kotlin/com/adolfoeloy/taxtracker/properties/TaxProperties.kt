package com.adolfoeloy.taxtracker.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("tax")
data class TaxProperties(
    val currencyBeacon: CurrencyBeacon? = null,
    val skipPaidFor: List<String> = emptyList()
)

data class CurrencyBeacon(
    val baseUrl: String,
    val apiKey: String,
    val enabled: Boolean = false
)