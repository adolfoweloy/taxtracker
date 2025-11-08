package com.adolfoeloy.taxtracker.transaction

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Component
import java.io.InputStream

interface CsvTransactionData {

    fun loadFrom(file: InputStream): List<TransactionRequest>

}

@Component
class CsvTransactionDataImpl : CsvTransactionData {
    private val csvMapper = CsvMapper().registerKotlinModule()

    override fun loadFrom(file: InputStream): List<TransactionRequest> {
        val schema = CsvSchema.emptySchema()
            .withHeader()
            .withColumnSeparator(';')

        val result: List<TransactionRequest> = csvMapper
            .readerFor(TransactionRequest::class.java)
            .with(schema)
            .readValues<TransactionRequest>(file)
            .readAll()

        return result
    }
}

data class TransactionRequest(
    @JsonProperty("Produto")
    val product: String = "",

    @JsonProperty("Certificado|Evento")
    val certificate: String = "",

    @JsonProperty("Emissão")
    val issuedAt: String = "",

    @JsonProperty("Vencimento")
    val matureAt: String = "",

    @JsonProperty("Pagamento")
    val paymentAt: String = "",

    @JsonProperty("Taxa|%")
    val percentage: String = "",

    @JsonProperty("Principal")
    val principal: String = "",

    @JsonProperty("Resgate")
    val redemption: String = "",

    @JsonProperty("Rendimento")
    val interest: String = "",

    @JsonProperty("IOF")
    val iof: String = "",

    @JsonProperty("IR")
    val brTax: String = "",

    @JsonProperty("Crédito")
    val credit: String = "",

    @JsonProperty("Descrição")
    val description: String = "",

    @JsonProperty("BRLAUD Rate")
    val brToAuForex: String = ""
)
