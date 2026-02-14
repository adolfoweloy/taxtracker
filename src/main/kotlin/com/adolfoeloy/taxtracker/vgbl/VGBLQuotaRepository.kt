package com.adolfoeloy.taxtracker.vgbl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface VGBLQuotaRepository : JpaRepository<VGBLQuota, VGBLQuotaId> {

    @Query("""
        SELECT f.fundName AS fundName, q.id.cnpj AS cnpj, q.id.competenceDate AS competenceDate, q.quotaValue AS quotaValue
        FROM VGBLQuota q JOIN VGBLFund f ON q.id.cnpj = f.cnpj
        ORDER BY f.fundName, q.id.competenceDate DESC
    """)
    fun findAllWithFundName(): List<QuotaWithFundName>
}

interface QuotaWithFundName {
    val fundName: String
    val cnpj: String
    val competenceDate: LocalDate
    val quotaValue: BigDecimal
}

