package com.adolfoeloy.taxtracker.forex

import com.adolfoeloy.taxtracker.forex.provider.ForexProvider
import com.adolfoeloy.taxtracker.util.fromCentsToBigDecimal
import com.adolfoeloy.taxtracker.util.toCents
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DefaultForexService(
    private val forexProvider: ForexProvider
) : ForexService {

    /**
     * Apply the forex rate to the given amount in cents for the specified currency ticker and date.
     * @param amount Amount in cents (e.g., 123456 for R$1,234.56)
     * @param date Date for which the forex rate should be applied
     * @param currencyTicker Currency ticker (e.g., "USD", "EUR")
     * @return Amount in cents after applying the forex rate, considering 2 decimal places for the amount.
     */
    override fun applyForexRateFor(
        amount: Int,
        date: LocalDate,
        currencyTicker: String
    ): Int {
        val forexRate = forexProvider.getRate(currencyTicker, date).rate
        val bigDecimalForexRate = forexRate.fromCentsToBigDecimal(forexProvider.getRateScale())

        return amount.fromCentsToBigDecimal(scale = 2)
            .multiply(bigDecimalForexRate)
            .toCents(scale = 2)
    }

}
