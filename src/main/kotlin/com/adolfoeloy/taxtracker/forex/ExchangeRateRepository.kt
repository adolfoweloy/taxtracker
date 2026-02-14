package com.adolfoeloy.taxtracker.forex

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ExchangeRateRepository : JpaRepository<ExchangeRate, Int> {

    fun findBySourceAndTargetAndRateAt(
        source: String,
        target: String,
        rateAt: LocalDate
    ): ExchangeRate?

    fun findAllByOrderByRateAtDesc(pageable: Pageable): Page<ExchangeRate>

    fun findByRateAtOrderBySourceAsc(rateAt: LocalDate, pageable: Pageable): Page<ExchangeRate>

}