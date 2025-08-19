package com.adolfoeloy.taxtracker.report

data class BalanceReport(
    val product: String,
    val certificate: String,
    val issuedAt: String,
    val maturityDate: String,
    val principal: String,
    val interest: String,
    val estimatedBrlTax: String
)