package com.adolfoeloy.taxtracker.vgbl.v2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvestmentFundDailyInfoRepository :
    JpaRepository<InvestmentFundDailyInfo, InvestmentFundDailyInfoId> {
}
