package com.adolfoeloy.taxtracker.forex.provider

import java.time.LocalDate

interface ForexProvider {

    /**
     * Get the forex rate for a given currency ticker and date.
     * @throws ForexRateException
     */
    fun getRate(ticker: String, date: LocalDate): ForexRate

    /**
     * Get the scale used for the forex rates.
     * E.g., if the rate is 1.234567, the scale is 6 which this means the rate is represented as 1234567.
     */
    fun getRateScale(): Int

}