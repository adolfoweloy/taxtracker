package com.adolfoeloy.taxtracker.report

data class TransactionReport(
    val product: String,
    val certificate: String,
    val issuedAt: String,
    val matureAt: String,
    val paymentAt: String,
    val principal: String,
    val redemption: String,
    val interest: String,
    val brTax: String,
    val credit: String,
    val description: String,
    val brToAuForex: String
)