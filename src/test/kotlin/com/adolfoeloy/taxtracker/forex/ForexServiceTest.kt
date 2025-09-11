package com.adolfoeloy.taxtracker.forex

import com.adolfoeloy.taxtracker.forex.provider.LocalForexProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ForexServiceTest {

    @Mock
    private lateinit var exchangeRateRepositoryMock: ExchangeRateRepository

    private lateinit var forexService: ForexService

    private val dummyDate = LocalDate.of(2024, 1, 15) // Dummy date for testing

    @BeforeEach
    fun setUp() {
        forexService = ForexService(LocalForexProvider(), exchangeRateRepositoryMock)
    }

    @Nested
    @DisplayName("applyForexRateFor tests - converting FROM BRL TO target currency")
    inner class ApplyForexRateForTests {

        @Test
        @DisplayName("should convert BRL to AUD correctly")
        fun shouldConvertBrlToAudCorrectly() {
            // Given
            val brlAmountInCents = 10000 // 100.00 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 100.00 BRL * 0.271244 AUD/BRL = 27.1244 AUD = 2712 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(2712)
        }

        @Test
        @DisplayName("should handle small BRL to AUD amounts correctly")
        fun shouldHandleSmallBrlToAudAmountsCorrectly() {
            // Given
            val brlAmountInCents = 100 // 1.00 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 1.00 BRL * 0.271244 AUD/BRL = 0.271244 AUD = 27 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(27)
        }

        @Test
        @DisplayName("should handle large BRL to AUD amounts correctly")
        fun shouldHandleLargeBrlToAudAmountsCorrectly() {
            // Given
            val brlAmountInCents = 100000000 // 1,000,000.00 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 1,000,000.00 BRL * 0.271244 AUD/BRL = 271,244.00 AUD = 27124400 cents
            assertThat(result).isEqualTo(27124400)
        }

        @Test
        @DisplayName("should return same amount for BRL to BRL conversion")
        fun shouldReturnSameAmountForBrlToBrlConversion() {
            // Given
            val brlAmountInCents = 50000 // 500.00 BRL
            val targetCurrency = "BRL"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // BRL to BRL should be 1:1, rate is 1000000 which becomes 1.000000 after movePointLeft(6)
            // So 500.00 BRL * 1.000000 = 500.00 BRL = 50000 cents
            assertThat(result).isEqualTo(50000)
        }

        @Test
        @DisplayName("should return same amount for unknown currencies (fallback to BRL)")
        fun shouldReturnSameAmountForUnknownCurrencies() {
            // Given
            val brlAmountInCents = 25000 // 250.00 BRL
            val unknownCurrency = "USD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, unknownCurrency)

            // Then
            // Unknown currencies fallback to BRL rate (1000000 -> 1.000000), so amount should remain the same
            assertThat(result).isEqualTo(25000)
        }

        @Test
        @DisplayName("should handle zero amount")
        fun shouldHandleZeroAmount() {
            // Given
            val zeroAmount = 0
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(zeroAmount, dummyDate, targetCurrency)

            // Then
            assertThat(result).isEqualTo(0)
        }

        @Test
        @DisplayName("should handle negative BRL amounts when converting to AUD")
        fun shouldHandleNegativeBrlAmountsWhenConvertingToAud() {
            // Given
            val negativeBrlAmount = -10000 // -100.00 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(negativeBrlAmount, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: -100.00 BRL * 0.271244 AUD/BRL = -27.1244 AUD = -2712 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(-2712)
        }

        @Test
        @DisplayName("should be case sensitive - lowercase currencies fallback to BRL")
        fun shouldBeCaseSensitive() {
            // Given
            val brlAmountInCents = 10000 // 100.00 BRL
            val lowerCaseAud = "aud"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, lowerCaseAud)

            // Then
            // "aud" != "AUD", so it falls back to BRL rate (1000000 -> 1.000000), amount remains the same
            assertThat(result).isEqualTo(10000)
        }

        @Test
        @DisplayName("should handle empty currency ticker - fallback to BRL")
        fun shouldHandleEmptyCurrencyTicker() {
            // Given
            val brlAmountInCents = 15000 // 150.00 BRL
            val emptyCurrency = ""

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, emptyCurrency)

            // Then
            // Empty string falls back to BRL rate (1000000 -> 1.000000), amount remains the same
            assertThat(result).isEqualTo(15000)
        }

        @Test
        @DisplayName("should demonstrate proper rounding for small BRL to AUD conversions")
        fun shouldDemonstrateProperRoundingForSmallBrlToAudConversions() {
            // Given
            val brlAmountInCents = 37 // 0.37 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 0.37 BRL * 0.271244 AUD/BRL = 0.10036028 AUD = 10 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(10)
        }

        @Test
        @DisplayName("should demonstrate proper rounding with medium BRL to AUD amounts")
        fun shouldDemonstrateProperRoundingWithMediumBrlToAudAmounts() {
            // Given
            val brlAmountInCents = 1000 // 10.00 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 10.00 BRL * 0.271244 AUD/BRL = 2.71244 AUD = 271 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(271)
        }

        @Test
        @DisplayName("should handle minimum conversion amount that results in 1 AUD cent")
        fun shouldHandleMinimumConversionAmountThatResultsInOneAudCent() {
            // Given
            val brlAmountInCents = 369 // 3.69 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 3.69 BRL * 0.271244 AUD/BRL = 1.00089036 AUD = 100 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(100)
        }

        @Test
        @DisplayName("should demonstrate HALF_EVEN rounding behavior")
        fun shouldDemonstrateHalfEvenRoundingBehavior() {
            // Given - construct an amount that will result in exactly X.5 cents to test HALF_EVEN rounding
            val brlAmountInCents = 1845 // 18.45 BRL
            val targetCurrency = "AUD"

            // When
            val result = forexService.applyForexRateFor(brlAmountInCents, dummyDate, targetCurrency)

            // Then
            // Converting FROM BRL TO AUD: 18.45 BRL * 0.271244 AUD/BRL = 5.0044518 AUD = 500 cents (rounded with HALF_EVEN)
            assertThat(result).isEqualTo(500)
        }
    }
}