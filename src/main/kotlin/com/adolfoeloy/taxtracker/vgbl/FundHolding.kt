package com.adolfoeloy.taxtracker.vgbl

import java.math.BigDecimal

data class FundHolding(
    val cnpj: String,
    val fundName: String,
    val quotas: BigDecimal
)
