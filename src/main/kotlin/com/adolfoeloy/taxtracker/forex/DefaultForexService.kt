package com.adolfoeloy.taxtracker.forex

import com.adolfoeloy.taxtracker.forex.provider.ForexProvider
import com.adolfoeloy.taxtracker.util.fromCentsToBigDecimal
import com.adolfoeloy.taxtracker.util.toCents
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class DefaultForexService(
    private val forexProvider: ForexProvider,
    private val exchangeRateRepository: ExchangeRateRepository
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
        val forexRate = getForexRateFor(currencyTicker, date)
        val bigDecimalForexRate = forexRate.fromCentsToBigDecimal(forexProvider.getRateScale())

        return amount.fromCentsToBigDecimal(scale = 2)
            .multiply(bigDecimalForexRate)
            .toCents(scale = 2)
    }

    /**
     * TODO: this should be implemented as a decorator around ForexService.
     */
    override fun getForexRateFor(
        currencyTicker: String,
        date: LocalDate
    ): Int {
        val exchangeRateFromDB = exchangeRateRepository.findBySourceAndTargetAndRateAt(
            source = "BRL",
            target = currencyTicker,
            rateAt = date
        )

        if (exchangeRateFromDB != null) {
            return exchangeRateFromDB.rate
        } else {
            val exchangeRateFromProvider = forexProvider.getRate(currencyTicker, date)
            val exchangeRateToSave = ExchangeRate().apply {
                source = "BRL"
                target = currencyTicker
                rateAt = date
                rate = exchangeRateFromProvider.rate
            }
            exchangeRateRepository.save(exchangeRateToSave)
            return exchangeRateFromProvider.rate
        }
    }
}
