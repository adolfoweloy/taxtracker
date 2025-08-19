package com.adolfoeloy.taxtracker.transaction

import com.opencsv.bean.CsvBindByName

data class TransactionRequest(
    @CsvBindByName(column = "Produto")
    val product: String = "",

    @CsvBindByName(column = "Certificado|Evento")
    val certificate: String = "",

    @CsvBindByName(column = "Emissão")
    val issuedAt: String = "",

    @CsvBindByName(column = "Vencimento")
    val matureAt: String = "",

    @CsvBindByName(column = "Pagamento")
    val paymentAt: String = "",

    @CsvBindByName(column = "Taxa|%")
    val percentage: String = "",

    @CsvBindByName(column = "Principal")
    val principal: String = "",

    @CsvBindByName(column = "Resgate")
    val redemption: String = "",

    @CsvBindByName(column = "Rendimento")
    val interest: String = "",

    @CsvBindByName(column = "IOF")
    val iof: String = "",

    @CsvBindByName(column = "IR")
    val brTax: String = "",

    @CsvBindByName(column = "Crédito")
    val credit: String = "",

    @CsvBindByName(column = "Descrição")
    val description: String = "",

    @CsvBindByName(column = "BRLAUD Rate")
    val brToAuForex: String = ""
)
