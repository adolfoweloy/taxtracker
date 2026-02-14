package com.adolfoeloy.taxtracker.forex

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Entity
@Table(name = "exchange_rate")
class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    var source: String = ""

    var target: String = ""

    @Column(name = "rate_at")
    var rateAt: LocalDate = LocalDate.now()

    var rate: Int = 0

    var scale: Int = 6

    @Transient
    fun getFormattedRate(): BigDecimal =
        BigDecimal(rate).divide(BigDecimal.TEN.pow(scale), scale, RoundingMode.HALF_EVEN)
}