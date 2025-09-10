package com.adolfoeloy.taxtracker.forex.provider

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate

@ConditionalOnProperty(
    prefix = "tax.currencyBeacon",
    name = ["enabled"],
    havingValue = "false"
)
@Component
class LocalForexProvider : ForexProvider {

    override fun getRate(ticker: String, date: LocalDate): ForexRate? {
        return when (ticker) {
            "AUD" -> ForexRate("AUD", 271_244) // 0.271244 BRL
            else -> ForexRate("BRL", 1_000_000) // Default rate for BRL or unknown currencies
        }
    }

}