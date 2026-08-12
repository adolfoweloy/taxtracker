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

    @Test
    fun `should pick the latest date even when the rows are not ordered by date`() {
        val csv = csvOf(
            row(CNPJ, "2024-04-30", "1.400000000000"),
            row(CNPJ, "2024-04-15", "1.200000000000"),
            row(CNPJ, "2024-04-29", "1.300000000000")
        )

        val result = subject.loadFrom(CNPJ, csv)

        assertThat(result?.date).isEqualTo("2024-04-30")
        assertThat(result?.quotaValue).isEqualTo("1.400000000000")
    }

    @Test
    fun `should pick the latest date across a file spanning several months`() {
        val csv = csvOf(
            row(CNPJ, "2024-05-31", "1.500000000000"),
            row(CNPJ, "2024-04-30", "1.400000000000")
        )

        val result = subject.loadFrom(CNPJ, csv)

        assertThat(result?.date).isEqualTo("2024-05-31")
    }

    @Test
    fun `should ignore rows belonging to other funds`() {
        val csv = csvOf(
            row(CNPJ, "2024-04-29", "1.300000000000"),
            row(OTHER_CNPJ, "2024-04-30", "9.900000000000")
        )

        val result = subject.loadFrom(CNPJ, csv)

        assertThat(result?.date).isEqualTo("2024-04-29")
        assertThat(result?.quotaValue).isEqualTo("1.300000000000")
    }

    @Test
    fun `should return null when the fund is absent`() {
        val csv = csvOf(row(OTHER_CNPJ, "2024-04-30", "9.900000000000"))

        assertThat(subject.loadFrom(CNPJ, csv)).isNull()
    }

    @Test
    fun `should return null rather than a wrong row when no date can be parsed`() {
        // A change in CVM's date format must fail visibly, not resolve to a plausible wrong day.
        val csv = csvOf(
            row(CNPJ, "30/04/2024", "1.400000000000"),
            row(CNPJ, "29/04/2024", "1.300000000000")
        )

        assertThat(subject.loadFrom(CNPJ, csv)).isNull()
    }

    @Test
    fun `should skip unparseable rows but still use the ones that parse`() {
        val csv = csvOf(
            row(CNPJ, "2024-04-29", "1.300000000000"),
            row(CNPJ, "not-a-date", "9.900000000000")
        )

        val result = subject.loadFrom(CNPJ, csv)

        assertThat(result?.date).isEqualTo("2024-04-29")
    }

    private fun row(cnpj: String, date: String, quotaValue: String) =
        "CLASSES - FIF;$cnpj;;$date;1000.00;$quotaValue;1000.00;0.00;0.00;1"

    private fun csvOf(vararg rows: String) =
        (listOf(HEADER) + rows).joinToString("\n").byteInputStream()

    private companion object {
        const val CNPJ = "12.345.678/0001-90"
        const val OTHER_CNPJ = "98.765.432/0001-09"
        const val HEADER =
            "TP_FUNDO_CLASSE;CNPJ_FUNDO_CLASSE;ID_SUBCLASSE;DT_COMPTC;VL_TOTAL;VL_QUOTA;" +
                "VL_PATRIM_LIQ;CAPTC_DIA;RESG_DIA;NR_COTST"
    }
}