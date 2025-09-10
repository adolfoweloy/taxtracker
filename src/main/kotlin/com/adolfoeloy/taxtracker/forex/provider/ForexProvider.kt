package com.adolfoeloy.taxtracker.forex.provider

import java.time.LocalDate

interface ForexProvider {

    fun getRate(ticker: String, date: LocalDate): ForexRate?

}