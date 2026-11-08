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
            quota_balance AS (
                SELECT
                    cnpj,
                    transaction_date,
                    transaction_type,
                    SUM(
                        CASE transaction_type
                            WHEN 'CONTRIBUTION' THEN quotas
                            WHEN 'REDEMPTION' THEN -quotas
                        END
                    ) OVER (
                        PARTITION BY cnpj
                        ORDER BY transaction_date, id
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS quotas_held
                FROM vgbl_track
                WHERE cnpj = :cnpj
            ),
            monthly_income AS (
                SELECT DISTINCT ON (DATE_TRUNC('month', vq.competence_date))
                    vq.competence_date,
                    (
                        COALESCE(
                            (
                                SELECT qb.quotas_held
                                FROM quota_balance qb
                                -- a redemption is valued on its own date, so that day is still
                                -- priced with the quotas held before it
                                WHERE qb.transaction_date < vq.competence_date
                                   OR (qb.transaction_date = vq.competence_date AND qb.transaction_type = 'CONTRIBUTION')
                                ORDER BY qb.transaction_date DESC
                                LIMIT 1
                            ),
                            f.quotas
                        ) * vq.quota_value
                    ) AS income
                FROM vgbl_quota vq
                INNER JOIN fund f ON f.cnpj = vq.cnpj
                LEFT JOIN redemption r ON r.cnpj = f.cnpj
                WHERE f.cnpj = :cnpj
                AND vq.competence_date >= :startDate
                AND vq.competence_date < :endDate
                -- the redemption date is its valuation date, the last day the fund was held
                AND (r.transaction_date IS NULL OR vq.competence_date <= r.transaction_date)
                ORDER BY DATE_TRUNC('month', vq.competence_date), vq.competence_date DESC
            ),
            priced AS (
                SELECT
                    competence_date,
                    income,
                    LAG(income) OVER w AS previous_income,
                    LAG(competence_date) OVER w AS previous_date
                FROM monthly_income
                WINDOW w AS (ORDER BY competence_date)
            )
            SELECT
                p.competence_date,
                p.income,
                p.previous_income,
                -- money that moved the quota count between the two pricing dates, so it is not
                -- mistaken for income: a redemption counts from the day after it, a contribution
                -- from its own day, matching how quota_balance is looked up above
                COALESCE((
                    SELECT SUM(
                        CASE t.transaction_type
                            WHEN 'REDEMPTION' THEN t.amount
                            WHEN 'CONTRIBUTION' THEN -t.amount
                        END
                    )
                    FROM vgbl_track t
                    WHERE t.cnpj = :cnpj
                    AND (
                        (t.transaction_type = 'REDEMPTION'
                            AND t.transaction_date >= p.previous_date
                            AND t.transaction_date < p.competence_date)
                        OR (t.transaction_type = 'CONTRIBUTION'
                            AND t.transaction_date > p.previous_date
                            AND t.transaction_date <= p.competence_date)
                    )
                ), 0) AS net_flow
            FROM priced p
            ORDER BY p.competence_date
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

    /**
     * Redemptions minus contributions between the previous row's date and this one. Zero for the
     * first row, which has no previous date to measure from.
     */
    val netFlow: BigDecimal
}