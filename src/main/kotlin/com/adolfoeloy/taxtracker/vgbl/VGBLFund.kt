package com.adolfoeloy.taxtracker.vgbl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "fund")
class VGBLFund {
    @Id
    var cnpj: String = ""

    @Column(name = "plan_name")
    var planName: String = ""

    @Column(name = "fund_name")
    var fundName: String = ""

    @Column(name = "quotas", precision = 27, scale = 12)
    var quotas: BigDecimal = BigDecimal.ZERO
}
