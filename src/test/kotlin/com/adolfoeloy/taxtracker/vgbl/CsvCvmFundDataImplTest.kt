package com.adolfoeloy.taxtracker.vgbl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CsvCvmFundDataImplTest {

    private val subject = CsvCvmFundDataImpl()

    @Test
    fun `CSV file from CVM should load correct CvmFundData properties for Bradesco VGBL TRUXT Macro`() {
        val resourcePath = "/inf_diario_fi_202508_cvm.csv"
        val file = File(this::class.java.getResource(resourcePath)?.file
            ?: throw IllegalStateException("Resource file not found"))

        val cvmFundData = subject.loadFrom("26.756.416/0001-28", file.inputStream())

        val expected = CvmFundData(
            fundType = "CLASSES - FIF",
            cnpj = "26.756.416/0001-28",
            subclassId = "",
            date = "2025-08-29",
            totalValue = "42574256.30",
            quotaValue = "1.300695300000",
            netAssetValue = "42548186.57",
            dailyCaptation = "406.74",
            dailyRedemption = "136058.63",
            numberOfShareholders = "1"
        )

        assertThat(cvmFundData)
            .usingRecursiveAssertion()
            .isEqualTo(expected)
    }
}