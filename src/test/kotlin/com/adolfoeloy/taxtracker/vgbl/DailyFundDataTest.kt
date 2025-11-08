package com.adolfoeloy.taxtracker.vgbl

import org.junit.jupiter.api.Test

class DailyFundDataTest {

    @Test
    fun `fund data should group CVM data by CNPJ`() {
        val dailyFundData = DailyFundData.fromCvmData(listOf(
            createCvmFundData("00.017.024/0001-53"),
            createCvmFundData("01.147.641/0001-36"),
            createCvmFundData("01.147.641/0001-36")
        ))

        assert(dailyFundData.cnpjToDailyFundData.size == 2)
        assert(dailyFundData.cnpjToDailyFundData["00.017.024/0001-53"]!!.size == 1)
        assert(dailyFundData.cnpjToDailyFundData["01.147.641/0001-36"]!!.size == 2)
    }

    private fun createCvmFundData(cnpj: String): CvmFundData {
        return CvmFundData(
            fundType = "CLASSES - FIF",
            cnpj = cnpj,
            subclassId = "",
            date = "2025-08-04",
            totalValue = "1150344.36",
            quotaValue = "39.661528900000",
            netAssetValue = "1156638.94",
            dailyCaptation = "0.00",
            dailyRedemption = "0.00",
            numberOfShareholders = "1"
        )
    }
}
