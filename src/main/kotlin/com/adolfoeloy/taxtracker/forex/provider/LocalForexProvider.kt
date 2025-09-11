package com.adolfoeloy.taxtracker.forex.provider

import java.time.LocalDate
import kotlin.math.pow

class LocalForexProvider : ForexProvider {

    override fun getRate(ticker: String, date: LocalDate): ForexRate {
        return when (ticker) {
            "AUD" -> ForexRate("AUD", 271_244) // 0.271244 BRL
            else -> ForexRate("BRL", 10.toDouble().pow(getRateScale().toDouble()).toInt()) // Default rate for BRL or unknown currencies
        }
    }

    override fun getRateScale() = 6

}