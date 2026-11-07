package com.adolfoeloy.taxtracker.vgbl.v2

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Embeddable
data class InvestmentFundDailyInfoId(
    @Column(name = "cnpj")
    var cnpj: String = "",

    @Column(name = "competence_date")
    var competenceDate: LocalDate = LocalDate.now()
) : Serializable

@Entity
@Table(name = "investment_fund_daily_info")
class InvestmentFundDailyInfo {
    @EmbeddedId
    var id: InvestmentFundDailyInfoId = InvestmentFundDailyInfoId()

    @Column(name = "fund_class")
    var fundClass: String = ""

    // Precision and scale as defined by
    // https://dados.cvm.gov.br/dados/FI/DOC/INF_DIARIO/META/meta_inf_diario_fi.txt
    @Column(name = "quota_value", precision = 27, scale = 12)
    var quotaValue: BigDecimal = BigDecimal.ZERO
}
