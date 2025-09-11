package com.adolfoeloy.taxtracker.forex.provider

import com.adolfoeloy.taxtracker.properties.TaxProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ConditionalOnProperty(
    prefix = "tax.currencyBeacon",
    name = ["enabled"],
    havingValue = "true"
)
@Component
class CurrencyBeaconClient(
    private val taxProperties: TaxProperties,
    private val restTemplate: RestTemplate
) : ForexProvider {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun getRate(ticker: String, date: LocalDate): ForexRate {
        if (ticker == "BRL") {
            return ForexRate("BRL", 100_000_000, getRateScale()) // 1 BRL = 1 BRL
        }

        val currencyBeacon = taxProperties.currencyBeacon
            ?: throw IllegalStateException("CurrencyBeacon properties not configured")

        val url = "${currencyBeacon.baseUrl}/historical"
        val formattedDate = date.format(dateFormatter)

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${currencyBeacon.apiKey}")
        }

        val entity = HttpEntity<String>(headers)

        try {
            val response = restTemplate.exchange(
                "$url?base=BRL&symbols=$ticker&date=$formattedDate",
                HttpMethod.GET,
                entity,
                CurrencyBeaconResponse::class.java
            )

            if (response.statusCode.is2xxSuccessful) {
                val responseBody = response.body
                return if (responseBody?.response?.rates != null) {
                    val rate = responseBody.response.rates[ticker]
                    if (rate != null) {
                        // Convert the decimal rate to integer cents format (multiply by 100,000,000)
                        val rateInCents = (rate * 100_000_000).toInt()
                        ForexRate(ticker, rateInCents, getRateScale())
                    } else {
                        throw ForexRateException("Rate for $ticker not found in response")
                    }
                } else {
                    // TODO: log the details instead of having the response body in the exception
                    throw ForexRateException("Invalid response from CurrencyBeacon: ${response.body}")
                }
            } else {
                throw ForexRateException(
                    "Failed to fetch forex rate: ${response.statusCode} ${response.body}"
                )
            }


        } catch (e: Exception) {
            throw ForexRateException(
                message = "Error fetching forex rate for $ticker on $date: ${e.message}",
                cause = e
            )
        }
    }

    override fun getRateScale() = 8
}

data class CurrencyBeaconResponse(
    val meta: Meta?,
    val response: ResponseData?,
    val date: String?,
    val base: String?,
    val rates: Map<String, Double>?
)

data class Meta(
    val code: Int,
    val disclaimer: String?
)

data class ResponseData(
    val date: String?,
    val base: String?,
    val rates: Map<String, Double>?
)
