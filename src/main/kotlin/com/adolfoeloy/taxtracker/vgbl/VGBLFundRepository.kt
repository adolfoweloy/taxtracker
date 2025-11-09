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
                    (f.quotas * vq.quota_value) AS income,
                    DATE_TRUNC('month', vq.competence_date)::date as month_start
                FROM vgbl_quota vq
                INNER JOIN fund f ON f.cnpj = vq.cnpj
                WHERE f.cnpj = :cnpj
                AND EXTRACT(YEAR FROM vq.competence_date) = :year
                AND EXTRACT(MONTH FROM vq.competence_date) BETWEEN :startMonth AND :endMonth
            ),
            last_day_per_month AS (
                SELECT 
                    month_start,
                    MAX(competence_date) as last_saved_date
                FROM income_by_date
                GROUP BY month_start
            ),
            monthly_income AS (
                SELECT 
                    ibd.competence_date,
                    ibd.income
                FROM income_by_date ibd
                INNER JOIN last_day_per_month ldpm 
                    ON ibd.competence_date = ldpm.last_saved_date
            )
            SELECT 
                competence_date,
                income,
                LAG(income) OVER (ORDER BY competence_date) AS previous_income
            FROM monthly_income
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
    val previousIncome: BigDecimal
}