package com.adolfoeloy.taxtracker.vgbl.v2

import com.adolfoeloy.taxtracker.vgbl.CsvCvmFundData
import com.adolfoeloy.taxtracker.vgbl.CvmFundData
import com.adolfoeloy.taxtracker.vgbl.FundSummary
import com.adolfoeloy.taxtracker.vgbl.VGBLFundService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class InvestmentFundDailyImportServiceTest {

    @Mock
    private lateinit var vgblFundServiceMock: VGBLFundService

    @Mock
    private lateinit var csvCvmFundDataMock: CsvCvmFundData

    @Mock
    private lateinit var repositoryMock: InvestmentFundDailyInfoRepository

    private lateinit var subject: InvestmentFundDailyImportService

    private val truxt = FundSummary("26.756.416/0001-28", "TRUXT MACRO")
    private val other = FundSummary("11.111.111/0001-11", "OTHER FUND")
    private val file = ByteArrayInputStream(ByteArray(0))

    @BeforeEach
    fun setUp() {
        subject = InvestmentFundDailyImportService(vgblFundServiceMock, csvCvmFundDataMock, repositoryMock)
    }

    @Test
    fun `should report CNPJs that are not registered and import nothing`() {
        whenever(vgblFundServiceMock.findFundBy(truxt.cnpj)).thenReturn(truxt)
        whenever(vgblFundServiceMock.findFundBy("00.000.000/0001-00")).thenReturn(null)

        val result = subject.doImport(listOf(truxt.cnpj, "00.000.000/0001-00"), file)

        assertThat(result.imported).isEmpty()
        assertThat(result.errors).containsExactly("Funds do not exist: 00.000.000/0001-00")
        verifyNoInteractions(csvCvmFundDataMock, repositoryMock)
    }

    @Test
    fun `should report registered funds that have no data in the CVM file and import nothing`() {
        registered(truxt, other)
        whenever(csvCvmFundDataMock.loadAllFrom(eq(listOf(truxt.cnpj, other.cnpj)), any()))
            .thenReturn(mapOf(truxt.cnpj to listOf(cvm(truxt, "2025-08-01", "1.5"))))

        val result = subject.doImport(listOf(truxt.cnpj, other.cnpj), file)

        assertThat(result.imported).isEmpty()
        assertThat(result.errors).containsExactly("CVM fund data not found for CNPJs: [${other.cnpj}]")
        verify(repositoryMock, never()).save(any<InvestmentFundDailyInfo>())
    }

    @Test
    fun `should store every row of every fund and group the result by fund`() {
        registered(truxt, other)
        whenever(csvCvmFundDataMock.loadAllFrom(eq(listOf(truxt.cnpj, other.cnpj)), any())).thenReturn(
            mapOf(
                truxt.cnpj to listOf(cvm(truxt, "2025-08-01", "1.279672500000"), cvm(truxt, "2025-08-04", "1.3")),
                other.cnpj to listOf(cvm(other, "2025-08-01", "2.0"))
            )
        )

        val result = subject.doImport(listOf(truxt.cnpj, other.cnpj), file)

        val saved = argumentCaptor<InvestmentFundDailyInfo>()
        verify(repositoryMock, times(3)).save(saved.capture())
        assertThat(saved.allValues.map { it.id }).containsExactlyInAnyOrder(
            InvestmentFundDailyInfoId(truxt.cnpj, LocalDate.of(2025, 8, 1)),
            InvestmentFundDailyInfoId(truxt.cnpj, LocalDate.of(2025, 8, 4)),
            InvestmentFundDailyInfoId(other.cnpj, LocalDate.of(2025, 8, 1))
        )
        assertThat(saved.firstValue.fundClass).isEqualTo("CLASSES - FIF")
        assertThat(saved.firstValue.quotaValue).isEqualByComparingTo("1.279672500000")

        assertThat(result.errors).isEmpty()
        assertThat(result.imported.keys).containsExactly(truxt, other)
        assertThat(result.imported.getValue(truxt).map { it.competenceDate })
            .containsExactly(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 4))
    }

    @Test
    fun `should report the replaced value when re-importing a date with a different quota`() {
        val id = InvestmentFundDailyInfoId(truxt.cnpj, LocalDate.of(2025, 8, 1))
        registered(truxt)
        whenever(csvCvmFundDataMock.loadAllFrom(eq(listOf(truxt.cnpj)), any()))
            .thenReturn(mapOf(truxt.cnpj to listOf(cvm(truxt, "2025-08-01", "1.3"))))
        whenever(repositoryMock.findById(id)).thenReturn(Optional.of(stored(id, "1.2")))

        val imported = subject.doImport(listOf(truxt.cnpj), file).imported.getValue(truxt).single()

        assertThat(imported.quotaValue).isEqualByComparingTo("1.3")
        assertThat(imported.replacedQuotaValue).isEqualByComparingTo("1.2")
    }

    @Test
    fun `should not report a replacement when re-importing an unchanged quota`() {
        val id = InvestmentFundDailyInfoId(truxt.cnpj, LocalDate.of(2025, 8, 1))
        registered(truxt)
        whenever(csvCvmFundDataMock.loadAllFrom(eq(listOf(truxt.cnpj)), any()))
            .thenReturn(mapOf(truxt.cnpj to listOf(cvm(truxt, "2025-08-01", "1.3"))))
        whenever(repositoryMock.findById(id)).thenReturn(Optional.of(stored(id, "1.300000000000")))

        val imported = subject.doImport(listOf(truxt.cnpj), file).imported.getValue(truxt).single()

        assertThat(imported.replacedQuotaValue).isNull()
    }

    @Test
    fun `should not report a replacement for a date that was not stored yet`() {
        registered(truxt)
        whenever(csvCvmFundDataMock.loadAllFrom(eq(listOf(truxt.cnpj)), any()))
            .thenReturn(mapOf(truxt.cnpj to listOf(cvm(truxt, "2025-08-01", "1.3"))))

        val imported = subject.doImport(listOf(truxt.cnpj), file).imported.getValue(truxt).single()

        assertThat(imported.replacedQuotaValue).isNull()
    }

    private fun registered(vararg funds: FundSummary) =
        funds.forEach { whenever(vgblFundServiceMock.findFundBy(it.cnpj)).thenReturn(it) }

    private fun cvm(fund: FundSummary, date: String, quota: String) = CvmFundData(
        fundType = "CLASSES - FIF",
        cnpj = fund.cnpj,
        date = date,
        quotaValue = quota
    )

    private fun stored(id: InvestmentFundDailyInfoId, quota: String) = InvestmentFundDailyInfo().apply {
        this.id = id
        quotaValue = BigDecimal(quota)
    }
}
