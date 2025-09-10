package com.adolfoeloy.taxtracker.forex.provider

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate

@ConditionalOnProperty(
    prefix = "tax.currencyBeacon",
    name = ["enabled"],
    havingValue = "true"
)
@Component
class CurrencyBeaconClient : ForexProvider {
    override fun getRate(ticker: String, date: LocalDate): ForexRate? {
        TODO()
    }
}