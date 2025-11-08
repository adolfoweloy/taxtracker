package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import org.springframework.stereotype.Component

// the whole plan is, to import all data from CVM
// with data imported, create a service that will fetch the quotas and calculate the interest and taxes

@Component
class VGBLFundService(
    val vgblQuotaRepository: VGBLQuotaRepository
) {

    fun saveQuotaValue(
        cvmFundData: CvmFundData
    ): VGBLQuota {
        val quota = VGBLQuota().apply {
            id.cnpj = cvmFundData.cnpj
            id.competenceDate = cvmFundData.date.fromYYYYMMDDToLocalDate()
            fundClass = cvmFundData.fundType
            quotaValue = cvmFundData.quotaValue.fromStringToBigDecimal(scale = 12)
        }

        vgblQuotaRepository.save(quota)

        return quota
    }

}
