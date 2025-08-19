package com.adolfoeloy.taxtracker.report

import com.opencsv.bean.CsvBindByName

/**
 * Represents the Balance table from CDB Report -> Client Statement: Account Activity
 */
data class BalanceRequest(
    @CsvBindByName(column = "Produto")
    val product: String = "",

    @CsvBindByName(column = "Certificado|Evento")
    val certificate: String = "",

    @CsvBindByName(column = "Emissão")
    val issuedAt: String = "",

    @CsvBindByName(column = "Vencimento")
    val matureAt: String = "",

    @CsvBindByName(column = "Taxa|%")
    val percentage: String = "",

    @CsvBindByName(column = "Principal")
    val principal: String = "",

    @CsvBindByName(column = "Atualizado")
    val balance: String = "",

    @CsvBindByName(column = "Rendimento")
    val interest: String = "",

    @CsvBindByName(column = "IOF")
    val iof: String = "",

    @CsvBindByName(column = "IR")
    val brTax: String = "",

    @CsvBindByName(column = "Líquido")
    val balanceNet: String = "",

    @CsvBindByName(column = "Balance Date")
    val balanceDate: String = "",

    @CsvBindByName(column = "BRLAUD Rate")
    val brToAuForex: String = ""
)