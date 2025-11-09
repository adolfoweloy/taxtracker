package com.adolfoeloy.taxtracker.vgbl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.LocalDate

interface VGBLFundRepository : JpaRepository<VGBLFund, String> {

    @Query(
        value = """
            WITH income_by_date AS (
                SELECT
                    vq.competence_date,
                    (f.quotas * vq.quota_value) AS income
                FROM vgbl_quota vq
                INNER JOIN fund f ON f.cnpj = vq.cnpj
                WHERE f.cnpj = :cnpj
                AND EXTRACT(YEAR FROM vq.competence_date) = :year
                AND EXTRACT(MONTH FROM vq.competence_date) BETWEEN :startMonth AND :endMonth
            )
            SELECT
                competence_date,
                income,
                LAG(income) OVER (ORDER BY competence_date) AS previous_income,
                income - LAG(income) OVER (ORDER BY competence_date) AS income_difference
            FROM income_by_date
            ORDER BY competence_date
        """,
        nativeQuery = true
    )
    fun getIncomeDifferenceByCompetenceDate(
        cnpj: String,
        year: Int,
        startMonth: Int,
        endMonth: Int
    ): List<IncomeDifference>

}

interface IncomeDifference {
    val competenceDate: LocalDate
    val income: BigDecimal
    val previousIncome: BigDecimal?
    val incomeDifference: BigDecimal?
}