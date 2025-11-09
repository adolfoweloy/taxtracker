package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import com.adolfoeloy.taxtracker.util.fromYearMonthString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class VGBLFundServiceTest {

    @Mock
    private lateinit var vgblQuotaRepositoryMock: VGBLQuotaRepository

    @Mock // TODO: having to add another repository to this service is a smell that this service is doing too much
    private lateinit var vgblFundRepositoryMock: VGBLFundRepository

    @Mock
    private lateinit var forexServiceMock: ForexService

    private lateinit var subject: VGBLFundService

    @BeforeEach
    fun setUp() {
        subject = VGBLFundService(
            vgblQuotaRepository = vgblQuotaRepositoryMock,
            vgblFundRepository = vgblFundRepositoryMock,
            forexService = forexServiceMock
        )
    }

    @Test
    fun `the fund should be saved as the correct VGBL given the selected CvmFundData`() {
        val argumentCaptor = argumentCaptor<VGBLQuota>()

        val input = CvmFundData(
            fundType = "CLASSES - FIF",
            cnpj = "26.756.416/0001-28",
            subclassId = "",
            date = "2025-08-01",
            totalValue = "42637166.56",
            quotaValue = "1.279672500000",
            netAssetValue = "42645077.45",
            dailyCaptation = "601.80",
            dailyRedemption = "0.00",
            numberOfShareholders = "1"
        )

        subject.saveQuotaValue(input)
        verify(vgblQuotaRepositoryMock).save(argumentCaptor.capture())

        val expected = VGBLQuota().apply {
            id.cnpj = "26.756.416/0001-28"
            id.competenceDate = "2025-08-01".fromYYYYMMDDToLocalDate()
            fundClass = "CLASSES - FIF"
            quotaValue = "1.279672500000".fromStringToBigDecimal(scale = 12)
        }

        assertThat(argumentCaptor.firstValue)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `base month should return December when end month is January`() {
        val result = subject.previousMonth("202401".fromYearMonthString())
        assertThat(result).isEqualTo("202312".fromYearMonthString())
    }
}