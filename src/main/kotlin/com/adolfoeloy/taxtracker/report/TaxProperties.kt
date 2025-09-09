package com.adolfoeloy.taxtracker.report

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("tax")
data class TaxProperties(
    val skipPaidFor: List<String> = emptyList()
)
