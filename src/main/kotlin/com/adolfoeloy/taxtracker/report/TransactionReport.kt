package com.adolfoeloy.taxtracker.report

import java.math.BigDecimal

data class TransactionReport(
    val product: String,
    val certificate: String,
    val issuedAt: String,
    val matureAt: String,
    val paymentAt: String,
    val principal: Int,
    val redemption: Int,
    val interest: Int,
    val brTax: Int,
    val credit: Int,
    val description: String,
    val brToAuForex: BigDecimal
)