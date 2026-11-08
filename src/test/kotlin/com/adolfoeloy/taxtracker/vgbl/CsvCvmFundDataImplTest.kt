package com.adolfoeloy.taxtracker.vgbl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

class CsvCvmFundDataImplTest {

    private val subject = CsvCvmFundDataImpl()

    @Nested
    inner class LoadLatestFrom {
        @Test
        fun `CSV file from CVM should load correct CvmFundData properties for Bradesco VGBL TRUXT Macro`() {
            val resourcePath = "/inf_diario_fi_202508_cvm.csv"
            val file = File(this::class.java.getResource(resourcePath)?.file
                ?: throw IllegalStateException("Resource file not found"))

            val cvmFundData = subject.loadLatestFrom("26.756.416/0001-28", file.inputStream())

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

            val result = subject.loadLatestFrom(CNPJ, csv)

            assertThat(result?.date).isEqualTo("2024-04-30")
            assertThat(result?.quotaValue).isEqualTo("1.400000000000")
        }

        @Test
        fun `should pick the latest date across a file spanning several months`() {
            val csv = csvOf(
                row(CNPJ, "2024-05-31", "1.500000000000"),
                row(CNPJ, "2024-04-30", "1.400000000000")
            )

            val result = subject.loadLatestFrom(CNPJ, csv)

            assertThat(result?.date).isEqualTo("2024-05-31")
        }

        @Test
        fun `should ignore rows belonging to other funds`() {
            val csv = csvOf(
                row(CNPJ, "2024-04-29", "1.300000000000"),
                row(OTHER_CNPJ, "2024-04-30", "9.900000000000")
            )

            val result = subject.loadLatestFrom(CNPJ, csv)

            assertThat(result?.date).isEqualTo("2024-04-29")
            assertThat(result?.quotaValue).isEqualTo("1.300000000000")
        }

        @Test
        fun `should return null when the fund is absent`() {
            val csv = csvOf(row(OTHER_CNPJ, "2024-04-30", "9.900000000000"))

            assertThat(subject.loadLatestFrom(CNPJ, csv)).isNull()
        }

        @Test
        fun `should return null rather than a wrong row when no date can be parsed`() {
            // A change in CVM's date format must fail visibly, not resolve to a plausible wrong day.
            val csv = csvOf(
                row(CNPJ, "30/04/2024", "1.400000000000"),
                row(CNPJ, "29/04/2024", "1.300000000000")
            )

            assertThat(subject.loadLatestFrom(CNPJ, csv)).isNull()
        }

        @Test
        fun `should skip unparseable rows but still use the ones that parse`() {
            val csv = csvOf(
                row(CNPJ, "2024-04-29", "1.300000000000"),
                row(CNPJ, "not-a-date", "9.900000000000")
            )

            val result = subject.loadLatestFrom(CNPJ, csv)

            assertThat(result?.date).isEqualTo("2024-04-29")
        }
    }

    @Nested
    inner class LoadAllFrom {

        @Test
        fun `should load all entries from investment fund daily info for a given fund`() {
            val resourcePath = "/inf_diario_fi_202508_cvm.csv"
            val file = File(this::class.java.getResource(resourcePath)?.file
                ?: throw IllegalStateException("Resource file not found"))

            val result = subject.loadAllFrom(listOf("26.756.416/0001-28"), file.inputStream())

            fun data(date: String, total: String, quota: String, nav: String, captation: String, redemption: String) =
                CvmFundData(
                    fundType = "CLASSES - FIF",
                    cnpj = "26.756.416/0001-28",
                    subclassId = "",
                    date = date,
                    totalValue = total,
                    quotaValue = quota,
                    netAssetValue = nav,
                    dailyCaptation = captation,
                    dailyRedemption = redemption,
                    numberOfShareholders = "1"
                )

            val expected = listOf(
                data("2025-08-01", "42637166.56", "1.279672500000", "42645077.45", "601.80", "0.00"),
                data("2025-08-04", "42696162.47", "1.281377100000", "42702071.07", "363.89", "176.51"),
                data("2025-08-05", "42688740.65", "1.281251600000", "42692376.87", "480.84", "5993.45"),
                data("2025-08-06", "42741326.09", "1.282109100000", "42743199.29", "22249.01", "0.00"),
                data("2025-08-07", "42771450.08", "1.285771200000", "42814553.59", "1205.51", "51937.84"),
                data("2025-08-08", "42799937.50", "1.285928100000", "42841264.31", "21486.11", "0.00"),
                data("2025-08-11", "42927679.42", "1.288480700000", "42927267.10", "962.74", "0.00"),
                data("2025-08-12", "43239823.02", "1.297244700000", "43237629.32", "21020.34", "2643.07"),
                data("2025-08-13", "43167469.44", "1.296191100000", "43163484.82", "1003.20", "40028.57"),
                data("2025-08-14", "43237024.07", "1.297594400000", "43231249.18", "21031.40", "0.00"),
                data("2025-08-15", "43053771.31", "1.300494500000", "43046090.64", "116.51", "281893.91"),
                data("2025-08-18", "42982317.82", "1.298386100000", "42972852.06", "14495.74", "17946.61"),
                data("2025-08-19", "42649817.66", "1.288640200000", "42638569.32", "2150.78", "13871.63"),
                data("2025-08-20", "42821933.68", "1.293416400000", "42807964.55", "12308.65", "950.64"),
                data("2025-08-21", "42767397.55", "1.291575800000", "42751651.90", "9606.03", "5000.00"),
                data("2025-08-22", "43003192.43", "1.297684000000", "42985672.54", "35853.49", "4018.11"),
                data("2025-08-25", "43105259.71", "1.300525900000", "43085956.36", "7465.74", "1318.17"),
                data("2025-08-26", "42995473.20", "1.298782500000", "42974382.72", "11633.46", "65450.00"),
                data("2025-08-27", "43503605.25", "1.300330600000", "43480732.00", "458162.23", "3035.46"),
                data("2025-08-28", "42814041.97", "1.303912100000", "42789399.92", "12618.35", "823709.25"),
                data("2025-08-29", "42574256.30", "1.300695300000", "42548186.57", "406.74", "136058.63")
            )

            assertThat(result["26.756.416/0001-28"]).containsExactlyElementsOf(expected)
        }

        @Test
        fun `should not load entries belonging to other funds`() {
            val csv = csvOf(
                row(CNPJ, "2024-04-29", "1.300000000000"),
                row(OTHER_CNPJ, "2024-04-29", "9.900000000000"),
                row(CNPJ, "2024-04-30", "1.400000000000"),
                row(OTHER_CNPJ, "2024-04-30", "9.800000000000")
            )

            val result = subject.loadAllFrom(listOf(CNPJ), csv)

            assertThat(result.keys)
                .containsOnly(CNPJ)
            assertThat(result[CNPJ])
                .extracting<String> { it.date }
                .containsExactly("2024-04-29", "2024-04-30")
        }
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