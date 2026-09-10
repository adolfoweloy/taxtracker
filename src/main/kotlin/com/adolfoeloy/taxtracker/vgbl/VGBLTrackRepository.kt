package com.adolfoeloy.taxtracker.vgbl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VGBLTrackRepository : JpaRepository<VGBLTrack, Int> {

    fun findByVgblFund(vgblFund: VGBLFund): List<VGBLTrack>

}