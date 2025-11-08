package com.adolfoeloy.taxtracker.vgbl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

interface CsvCvmFundData {

    /** Loads CVM fund data from a CSV file located at the given path.
     *
     * @param file The file to load the CSV data from.
     * @return A DailyFundData object containing the loaded data.
     */
    fun loadFrom(file: File): DailyFundData

}

class CsvCvmFundDataImpl : CsvCvmFundData {
    private val csvMapper = CsvMapper().registerKotlinModule()

    override fun loadFrom(file: File): DailyFundData {
        val schema = CsvSchema.emptySchema()
            .withHeader()
            .withColumnSeparator(';')

        val cvmFundDataList: List<CvmFundData> = csvMapper
            .readerFor(CvmFundData::class.java)
            .with(schema)
            .readValues<CvmFundData>(file)
            .readAll()

        return DailyFundData.fromCvmData(cvmFundDataList)
    }
}

/**
 * Represents daily fund data grouped by CNPJ.
 *
 * @property cnpjToDailyFundData A map where the key is the CNPJ and the value is a list of CvmFundData entries for that CNPJ.
 */
class DailyFundData(
    val cnpjToDailyFundData: Map<String, List<CvmFundData>>
) {
    fun getByCnpj(cnpj: String): List<CvmFundData> {
        return cnpjToDailyFundData[cnpj] ?: emptyList()
    }

    companion object {
        fun fromCvmData(cvmData: List<CvmFundData>): DailyFundData {
            val groupedData = cvmData.groupBy { it.cnpj }
            return DailyFundData(groupedData)
        }
    }
}

/**
 * Data class representing CVM fund data.
 *
 * @property fundType The type of the fund (TP_FUNDO_CLASSE).
 * @property cnpj The CNPJ of the fund (CNPJ_FUNDO_CLASSE).
 * @property subclassId The subclass ID (ID_SUBCLASSE).
 * @property date The date of the data (DT_COMPTC).
 * @property totalValue The total value of the fund (VL_TOTAL).
 * @property quotaValue The value per quota (VL_QUOTA).
 * @property netAssetValue The net asset value (VL_PATRIM_LIQ).
 * @property dailyCaptation The daily captation amount (CAPTC_DIA).
 * @property dailyRedemption The daily redemption amount (RESG_DIA).
 * @property numberOfShareholders The number of shareholders (NR_COTST).
 */
data class CvmFundData(
    @JsonProperty("TP_FUNDO_CLASSE")
    val fundType: String = "",

    @JsonProperty("CNPJ_FUNDO_CLASSE")
    val cnpj: String = "",

    @JsonProperty("ID_SUBCLASSE")
    val subclassId: String = "",

    @JsonProperty("DT_COMPTC")
    val date: String = "",

    @JsonProperty("VL_TOTAL")
    val totalValue: String = "",

    @JsonProperty("VL_QUOTA")
    val quotaValue: String = "",

    @JsonProperty("VL_PATRIM_LIQ")
    val netAssetValue: String = "",

    @JsonProperty("CAPTC_DIA")
    val dailyCaptation: String = "",

    @JsonProperty("RESG_DIA")
    val dailyRedemption: String = "",

    @JsonProperty("NR_COTST")
    val numberOfShareholders: String = ""
)
