package com.adolfoeloy.taxtracker.vgbl

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

interface CsvCvmFundData {

    /** Loads CVM fund data from a CSV file located at the given path.
     *
     * @param cnpj The CNPJ of the fund to load data for.
     * @param inputStream The input stream to load the CSV data from.
     * @return A DailyFundData object containing the loaded data.
     */
    fun loadFrom(cnpj: String, inputStream: InputStream): CvmFundData?

}

@Component
class CsvCvmFundDataImpl : CsvCvmFundData {

    override fun loadFrom(cnpj: String, inputStream: InputStream): CvmFundData? {
        val row = BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.lastOrNull() { line -> // TODO: trusting that the lines are ordered by date, this should be improved.
                line.split(';')
                    .getOrNull(1)
                    ?.equals(cnpj)
                    ?: false
            }
        }

        return row?.let {
            row.split(";").let { columns ->
                CvmFundData(
                    fundType = columns.getOrNull(0) ?: "",
                    cnpj = columns.getOrNull(1) ?: "",
                    subclassId = columns.getOrNull(2) ?: "",
                    date = columns.getOrNull(3) ?: "",
                    totalValue = columns.getOrNull(4) ?: "",
                    quotaValue = columns.getOrNull(5) ?: "",
                    netAssetValue = columns.getOrNull(6) ?: "",
                    dailyCaptation = columns.getOrNull(7) ?: "",
                    dailyRedemption = columns.getOrNull(8) ?: "",
                    numberOfShareholders = columns.getOrNull(9) ?: ""
                )
            }
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
    val fundType: String = "",
    val cnpj: String = "",
    val subclassId: String = "",
    val date: String = "",
    val totalValue: String = "",
    val quotaValue: String = "",
    val netAssetValue: String = "",
    val dailyCaptation: String = "",
    val dailyRedemption: String = "",
    val numberOfShareholders: String = ""
)
