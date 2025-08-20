package com.adolfoeloy.taxtracker.forex

import com.adolfoeloy.taxtracker.util.fromCentsToBigDecimal
import com.adolfoeloy.taxtracker.util.toCents
import org.springframework.stereotype.Component

@Component
class ForexService {

    fun applyForexRateFor(
        amount: Int,
        currencyTicker: String
    ): Int {
        return amount.fromCentsToBigDecimal(scale = 2)
            .multiply(
                getForexRate(currencyTicker).getBigDecimalRate()
            )
            .toCents(scale = 2)
    }

    private fun getForexRate(currencyTicker: String): ForexRate {
        return when (currencyTicker) {
            "AUD" -> ForexRate("AUD", 271244) // 0.271244 BRL
            else -> ForexRate("BRL", 1_000_000) // Default rate for BRL or unknown currencies
        }
    }

}