package com.adolfoeloy.taxtracker.forex

import com.adolfoeloy.taxtracker.forex.provider.ForexProvider
import com.adolfoeloy.taxtracker.util.fromCentsToBigDecimal
import com.adolfoeloy.taxtracker.util.toCents
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ForexService(
    private val forexProvider: ForexProvider
) {

    fun applyForexRateFor(
        amount: Int,
        date: LocalDate,
        currencyTicker: String
    ): Int {
        return amount.fromCentsToBigDecimal(scale = 2)
            .multiply(
                forexProvider.getRate(currencyTicker, date)?.getBigDecimalRate()
                    ?: throw IllegalArgumentException("No forex rate found for $currencyTicker on $date")
            )
            .toCents(scale = 2)
    }

}