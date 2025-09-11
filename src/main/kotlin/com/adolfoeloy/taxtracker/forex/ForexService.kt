package com.adolfoeloy.taxtracker.forex

import java.time.LocalDate

/**
 * Service for handling foreign exchange operations.
 */
interface ForexService {

    /**
     * Apply the forex rate to the given amount in cents for the specified currency ticker and date.
     * @param amount Amount in cents (e.g., 123456 for R$1,234.56)
     * @param date Date for which the forex rate should be applied
     * @param currencyTicker Currency ticker (e.g., "USD", "EUR")
     * @return Amount in cents after applying the forex rate, considering 2 decimal places for the amount.
     */
    fun applyForexRateFor(
        amount: Int,
        date: LocalDate,
        currencyTicker: String
    ): Int

}
