package com.adolfoeloy.taxtracker.vgbl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.LocalDate

interface VGBLFundRepository : JpaRepository<VGBLFund, String> {

    @Query(
        value = """
            WITH redemption AS (
                SELECT DISTINCT ON (cnpj) cnpj, transaction_type, transaction_date
                FROM vgbl_track
                WHERE cnpj = :cnpj
                  AND transaction_type = 'REDEMPTION'
                  AND full_redemption = true
                ORDER BY cnpj, transaction_date DESC
                LIMIT 1
            ),
            monthly_income AS (
                SELECT DISTINCT ON (DATE_TRUNC('month', vq.competence_date))
                    vq.competence_date,
                    (f.quotas * vq.quota_value) AS income
                FROM vgbl_quota vq
                INNER JOIN fund f ON f.cnpj = vq.cnpj
                LEFT JOIN redemption r ON r.cnpj = f.cnpj
                WHERE f.cnpj = :cnpj
                AND vq.competence_date >= :startDate
                AND CASE
                    WHEN EXISTS (
                        SELECT 1 
                        FROM redemption r 
                        WHERE r.cnpj = vq.cnpj 
                        AND r.transaction_date <= vq.competence_date
                    )
                    THEN vq.competence_date < r.transaction_date
                    ELSE vq.competence_date < :endDate
                END
                ORDER BY DATE_TRUNC('month', vq.competence_date), vq.competence_date DESC
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
        startDate: LocalDate,
        endDate: LocalDate
    ): List<IncomeDifference>

}

interface IncomeDifference {
    val competenceDate: LocalDate
    val income: BigDecimal

    /** Null for the first row of the window: `LAG` has nothing to look back at. */
    val previousIncome: BigDecimal?
}