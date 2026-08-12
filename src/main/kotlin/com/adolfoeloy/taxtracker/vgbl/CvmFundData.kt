package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate

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

    /**
     * Returns the fund's latest day in the file, chosen by parsing `DT_COMPTC` rather than by
     * taking the last matching line.
     *
     * The previous implementation assumed the file was ordered by date. That holds for CVM's own
     * monthly files, but it fails silently: an out-of-order or concatenated file yields the wrong
     * day with no error, and since only one day per fund is ever stored, a wrong day becomes the
     * month's value. Rows whose date cannot be parsed are ignored, so a change in CVM's date
     * format surfaces as "no data found" rather than as a plausible-looking wrong row.
     */
    override fun loadFrom(cnpj: String, inputStream: InputStream): CvmFundData? {
        val latest = BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines
                .map { it.split(';') }
                .filter { it.getOrNull(CNPJ_COLUMN) == cnpj }
                .mapNotNull { columns -> competenceDate(columns)?.let { DatedRow(it, columns) } }
                // `>=` keeps the later line when two share the same date, matching what taking the
                // last matching line used to do.
                .reduceOrNull { best, current -> if (current.date >= best.date) current else best }
        }

        return latest?.columns?.let { columns ->
            CvmFundData(
                fundType = columns.getOrNull(0) ?: "",
                cnpj = columns.getOrNull(CNPJ_COLUMN) ?: "",
                subclassId = columns.getOrNull(2) ?: "",
                date = columns.getOrNull(DATE_COLUMN) ?: "",
                totalValue = columns.getOrNull(4) ?: "",
                quotaValue = columns.getOrNull(5) ?: "",
                netAssetValue = columns.getOrNull(6) ?: "",
                dailyCaptation = columns.getOrNull(7) ?: "",
                dailyRedemption = columns.getOrNull(8) ?: "",
                numberOfShareholders = columns.getOrNull(9) ?: ""
            )
        }
    }

    private fun competenceDate(columns: List<String>): LocalDate? =
        columns.getOrNull(DATE_COLUMN)?.let { runCatching { it.fromYYYYMMDDToLocalDate() }.getOrNull() }

    /** A row kept together with its parsed date, so the scan compares without re-parsing. */
    private data class DatedRow(val date: LocalDate, val columns: List<String>)

    private companion object {
        const val CNPJ_COLUMN = 1   // CNPJ_FUNDO_CLASSE
        const val DATE_COLUMN = 3   // DT_COMPTC
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
