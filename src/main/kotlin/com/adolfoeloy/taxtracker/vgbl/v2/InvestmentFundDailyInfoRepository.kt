package com.adolfoeloy.taxtracker.vgbl.v2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface InvestmentFundDailyInfoRepository :
    JpaRepository<InvestmentFundDailyInfo, InvestmentFundDailyInfoId> {

    fun findByIdCnpjAndIdCompetenceDateBetweenOrderByIdCompetenceDate(
        cnpj: String,
        start: LocalDate,
        end: LocalDate
    ): List<InvestmentFundDailyInfo>
}
