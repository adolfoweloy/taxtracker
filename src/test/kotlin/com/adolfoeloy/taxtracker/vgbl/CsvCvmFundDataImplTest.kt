package com.adolfoeloy.taxtracker.vgbl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CsvCvmFundDataImplTest {

    private val subject = CsvCvmFundDataImpl()

    @Test
    fun `CSV file from CVM should load correct CvmFundData properties for Bradesco VGBL TRUXT Macro`() {
        val resourcePath = "/inf_diario_fi_202508.csv"
        val file = File(this::class.java.getResource(resourcePath)?.file
            ?: throw IllegalStateException("Resource file not found"))

        val cvmFundDataList = subject.loadFrom(file)
        val result = cvmFundDataList.getByCnpj("26.756.416/0001-28")
        val firstEntry = result.first { it.date == "2025-08-01" }

        val expected = CvmFundData(
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

        assertThat(firstEntry)
            .usingRecursiveAssertion()
            .isEqualTo(expected)
    }
}