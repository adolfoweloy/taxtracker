package com.adolfoeloy.taxtracker.forex.provider

import com.adolfoeloy.taxtracker.forex.ExchangeRate
import com.adolfoeloy.taxtracker.forex.ExchangeRateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ForexProviderDBDecoratorTest {

    @Mock
    private lateinit var decoratedForexProviderMock: ForexProvider

    @Mock
    private lateinit var exchangeRateRepositoryMock: ExchangeRateRepository

    private lateinit var forexProviderDBDecorator: ForexProviderDBDecorator

    @BeforeEach
    fun setUp() {
        forexProviderDBDecorator = ForexProviderDBDecorator(
            decorated = decoratedForexProviderMock,
            exchangeRateRepository = exchangeRateRepositoryMock
        )
    }

    @Test
    fun `should return rate from database when exchange rate is present in DB`() {
        // Arrange
        val ticker = "USD"
        val date = LocalDate.of(2025, 9, 11)
        val expectedRate = 26715019 // Rate stored in database as integer
        val expectedScale = 6

        val exchangeRateFromDB = ExchangeRate().apply {
            source = "BRL"
            target = ticker
            rateAt = date
            rate = expectedRate
        }

        whenever(decoratedForexProviderMock.getRateScale()).thenReturn(expectedScale)
        whenever(exchangeRateRepositoryMock.findBySourceAndTargetAndRateAt("BRL", ticker, date))
            .thenReturn(exchangeRateFromDB)

        // Act
        val result = forexProviderDBDecorator.getRate(ticker, date)

        // Assert
        assertThat(result.ticker).isEqualTo(ticker)
        assertThat(result.rate).isEqualTo(expectedRate)
        assertThat(result.scale).isEqualTo(expectedScale)

        verify(decoratedForexProviderMock, never()).getRate(ticker, date)
        verify(exchangeRateRepositoryMock, never()).save(any())
    }

    @Test
    fun `should fetch rate from provider and save to database when rate is not found in DB`() {
        // Arrange
        val ticker = "EUR"
        val date = LocalDate.of(2025, 9, 11)
        val providerRate = 35420875 // Rate from provider as integer
        val expectedScale = 6

        val forexRateFromProvider = ForexRate(ticker, providerRate, expectedScale)

        whenever(exchangeRateRepositoryMock.findBySourceAndTargetAndRateAt("BRL", ticker, date)).thenReturn(null)
        whenever(decoratedForexProviderMock.getRate(ticker, date)).thenReturn(forexRateFromProvider)
        whenever(decoratedForexProviderMock.getRateScale()).thenReturn(expectedScale)

        // Act
        val result = forexProviderDBDecorator.getRate(ticker, date)

        // Assert
        assertThat(result.ticker).isEqualTo(ticker)
        assertThat(result.rate).isEqualTo(providerRate)
        assertThat(result.scale).isEqualTo(expectedScale)

        verify(decoratedForexProviderMock).getRate(ticker, date)
        val savedExchangeRateCaptor = argumentCaptor<ExchangeRate>()

        verify(exchangeRateRepositoryMock).save(savedExchangeRateCaptor.capture())
        val savedExchangeRate = savedExchangeRateCaptor.firstValue

        assertThat(savedExchangeRate.source).isEqualTo("BRL")
        assertThat(savedExchangeRate.target).isEqualTo(ticker)
        assertThat(savedExchangeRate.rateAt).isEqualTo(date)
        assertThat(savedExchangeRate.rate).isEqualTo(providerRate)
    }
}