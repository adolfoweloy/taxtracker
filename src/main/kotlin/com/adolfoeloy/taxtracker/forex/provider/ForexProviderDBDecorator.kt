package com.adolfoeloy.taxtracker.forex.provider

import com.adolfoeloy.taxtracker.forex.ExchangeRate
import com.adolfoeloy.taxtracker.forex.ExchangeRateRepository
import java.time.LocalDate

class ForexProviderDBDecorator(
    private val decorated: ForexProvider,
    private val exchangeRateRepository: ExchangeRateRepository
) : ForexProvider {
    override fun getRate(
        ticker: String,
        date: LocalDate
    ): ForexRate {

        val exchangeRateFromDB = exchangeRateRepository.findBySourceAndTargetAndRateAt(
            source = "BRL",
            target = ticker,
            rateAt = date
        )

        if (exchangeRateFromDB != null) {
            return ForexRate(ticker, exchangeRateFromDB.rate, getRateScale())
        } else {
            val exchangeRateFromProvider = decorated.getRate(ticker, date)
            val exchangeRateToSave = ExchangeRate().apply {
                source = "BRL"
                target = ticker
                rateAt = date
                rate = exchangeRateFromProvider.rate
            }
            exchangeRateRepository.save(exchangeRateToSave)

            return ForexRate(ticker, exchangeRateFromProvider.rate, getRateScale())
        }
    }

    override fun getRateScale(): Int {
        return decorated.getRateScale()
    }
}