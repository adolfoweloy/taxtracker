package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import org.springframework.stereotype.Component

// the whole plan is, to import all data from CVM
// with data imported, create a service that will fetch the quotas and calculate the interest and taxes

@Component
class VGBLFundService(
    val vgblQuotaRepository: VGBLQuotaRepository,
    val vgblFundRepository: VGBLFundRepository
) {

    fun saveQuotaValue(
        cvmFundData: CvmFundData
    ): VGBLQuota {

        val vgblQuota = vgblQuotaRepository.findById(VGBLQuotaId(
            cnpj = cvmFundData.cnpj,
            competenceDate = cvmFundData.date.fromYYYYMMDDToLocalDate()
        ))

        if (vgblQuota.isPresent) {
            return vgblQuota.get()
        }

        val quota = VGBLQuota().apply {
            id.cnpj = cvmFundData.cnpj
            id.competenceDate = cvmFundData.date.fromYYYYMMDDToLocalDate()
            fundClass = cvmFundData.fundType
            quotaValue = cvmFundData.quotaValue.fromStringToBigDecimal(scale = 12)
        }

        vgblQuotaRepository.save(quota)

        return quota
    }

    fun saveFund(vgblFundService: VGBLFundRequest): VGBLFund {
        val fund = VGBLFund().apply {
            cnpj = vgblFundService.cnpj
            planName = vgblFundService.planName
            fundName = vgblFundService.fundName
            quotas = vgblFundService.quotas.fromStringToBigDecimal(scale = 12)
        }

        val savedFund = vgblFundRepository.save(fund)

        return savedFund
    }

}

data class VGBLFundRequest(
    val cnpj: String,
    val planName: String,
    val fundName: String,
    val quotas: String
)

