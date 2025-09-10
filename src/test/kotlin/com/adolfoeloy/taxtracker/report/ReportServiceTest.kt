package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.Balance
import com.adolfoeloy.taxtracker.balance.BalanceRepository
import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.forex.provider.LocalForexProvider
import com.adolfoeloy.taxtracker.product.Certificate
import com.adolfoeloy.taxtracker.product.Product
import com.adolfoeloy.taxtracker.properties.TaxProperties
import com.adolfoeloy.taxtracker.transaction.TransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month

@ExtendWith(MockitoExtension::class)
class ReportServiceTest {

    @Mock
    private lateinit var balanceRepository: BalanceRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var subject: ReportService

    @BeforeEach
    fun setUp() {
        subject = ReportService(balanceRepository, transactionRepository, ForexService(LocalForexProvider()), TaxProperties())
    }

    @Test
    fun `getReportData should return report data correctly for a month when there is no transactions`() {
        // Arrange
        val start = LocalDate.of(2024, Month.MAY, 1)
        val end = LocalDate.of(2024, Month.MAY, 31)
        // previous month
        whenever(balanceRepository.findByMonthAndYear(4, 2024))
            .thenReturn(balanceWithTwoProductsFor(start.month.value, start.year))
        // current month
        whenever(balanceRepository.findByMonthAndYear(5, 2024))
            .thenReturn(balanceWithTwoProductsFor(end.month.value, end.year, interestIncrement = 1_000))
        whenever(transactionRepository.findByMonthAndYear(5, 2024))
            .thenReturn(emptyList())

        // Act
        val result = subject.getTaxReportData(start, end, "BRL")

        // Assert
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].totalGrossInterestEarned).isEqualTo(2_000)
        assertThat(result[0].totalPaidTax).isZero // no transactions, so no tax paid
    }

    private fun balanceWithTwoProductsFor(month: Int, year: Int, interestIncrement: Int = 0): List<Balance> {
        val date = LocalDate.of(year, month, 1)
        return listOf(
            Balance().apply {
                product = Product().apply {
                    name = "Product A"
                    certificate = Certificate.createNormalizedCertificate("CERT123")
                    issuedAt = date.minusYears(5)
                    matureAt = date.plusYears(5)
                }
                principal = 1_000_000
                interest = 50_000 + interestIncrement
                brTax = toInt(interest * 0.15)
            },
            Balance().apply {
                product = Product().apply {
                    name = "Product A"
                    certificate = Certificate.createNormalizedCertificate("CERT456")
                    issuedAt = date.minusYears(3)
                    matureAt = date.plusYears(3)
                }
                principal = 2_000_000
                interest = 100_000 + interestIncrement
                brTax = toInt(interest * 0.15)
            }
        )
    }

    private fun toInt(value: Double): Int {
        return BigDecimal(value)
            .setScale(2, RoundingMode.HALF_EVEN)
            .movePointRight(2)
            .toInt()
    }
}