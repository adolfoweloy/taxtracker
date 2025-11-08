package com.adolfoeloy.taxtracker.balance

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Component
import java.io.InputStream

interface CsvBalanceData {

    fun loadFrom(file: InputStream): List<BalanceRequest>

}

@Component
class CsvBalanceDataImpl : CsvBalanceData {
    private val csvMapper = CsvMapper().registerKotlinModule()

    override fun loadFrom(file: InputStream): List<BalanceRequest> {
        val schema = CsvSchema.emptySchema()
            .withHeader()
            .withColumnSeparator(';')

        val result: List<BalanceRequest> = csvMapper
            .readerFor(BalanceRequest::class.java)
            .with(schema)
            .readValues<BalanceRequest>(file)
            .readAll()

        return result
    }
}

/**
 * Represents the Balance table from CDB Report -> Client Statement: Account Activity
 */
data class BalanceRequest(
    @JsonProperty("Produto")
    val product: String = "",

    @JsonProperty("Certificado|Evento")
    val certificate: String = "",

    @JsonProperty("Emissão")
    val issuedAt: String = "",

    @JsonProperty("Vencimento")
    val matureAt: String = "",

    @JsonProperty("Taxa|%")
    val percentage: String = "",

    @JsonProperty("Principal")
    val principal: String = "",

    @JsonProperty("Atualizado")
    val balance: String = "",

    @JsonProperty("Rendimento")
    val interest: String = "",

    @JsonProperty("IOF")
    val iof: String = "",

    @JsonProperty("IR")
    val brTax: String = "",

    @JsonProperty("Líquido")
    val balanceNet: String = "",

    @JsonProperty("Balance Date")
    val balanceDate: String = "",

    @JsonProperty("BRLAUD Rate")
    val brToAuForex: String = ""
)