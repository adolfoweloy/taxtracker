package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.forex.ExchangeRate
import com.adolfoeloy.taxtracker.forex.ExchangeRateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class ExchangeRateRepositoryIT : AbstractDatabaseIntegrationTest() {

    @Autowired
    private lateinit var subject: ExchangeRateRepository

    @Test
    fun `should create a new exchange rate correctly`() {
        // Arrange
        val exchangeRate = ExchangeRate().apply {
            source = "USD"
            target = "BRL"
            rateAt = LocalDate.of(2025, 9, 11)
            rate = 26715019
            scale = 6
        }

        // Act
        val saved = subject.save(exchangeRate)
        val retrievedExchangeRate = subject.findById(saved.id).orElse(null)

        // Assert
        assertThat(retrievedExchangeRate).isNotNull
        assertThat(retrievedExchangeRate?.source).isEqualTo("USD")
        assertThat(retrievedExchangeRate?.target).isEqualTo("BRL")
        assertThat(retrievedExchangeRate?.rateAt).isEqualTo(LocalDate.of(2025, 9, 11))
        assertThat(retrievedExchangeRate?.rate).isEqualTo(26715019)
        assertThat(retrievedExchangeRate?.id).isGreaterThan(0)
        assertThat(retrievedExchangeRate?.scale).isEqualTo(6)
    }

    @Test
    fun `should find exchange rate by source, target and date`() {
        // Arrange
        val exchangeRate = ExchangeRate().apply {
            source = "EUR"
            target = "BRL"
            rateAt = LocalDate.of(2025, 9, 10)
            rate = 30000000
            scale = 6
        }
        subject.save(exchangeRate)

        // Act
        val retrievedExchangeRate = subject.findBySourceAndTargetAndRateAt("EUR", "BRL", LocalDate.of(2025, 9, 10))

        // Assert
        assertThat(retrievedExchangeRate).isNotNull
        assertThat(retrievedExchangeRate?.source).isEqualTo("EUR")
        assertThat(retrievedExchangeRate?.target).isEqualTo("BRL")
        assertThat(retrievedExchangeRate?.rateAt).isEqualTo(LocalDate.of(2025, 9, 10))
        assertThat(retrievedExchangeRate?.rate).isEqualTo(30000000)
        assertThat(retrievedExchangeRate?.scale).isEqualTo(6)
    }

    @Test
    fun `should return null when exchange rate not found`() {
        // Act
        val retrievedExchangeRate = subject.findBySourceAndTargetAndRateAt("GBP", "BRL", LocalDate.of(2025, 9, 10))

        // Assert
        assertThat(retrievedExchangeRate).isNull()
    }
}