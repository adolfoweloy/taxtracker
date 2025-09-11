package com.adolfoeloy.taxtracker.forex

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
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
}