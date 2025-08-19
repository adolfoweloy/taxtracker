package com.adolfoeloy.taxtracker.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BalanceRepository : JpaRepository<Balance, Int> {

    /**
     * Finds a Balance by its product certificate and previous balance date.
     *
     * @param certificate the certificate of the product.
     * @param balanceDate the balance date.
     * @return the Balance if found, or null if not found.
     */
    @Query("SELECT b FROM Balance b JOIN b.product p WHERE p.certificate = :certificate AND b.balanceDate = :balanceDate")
    fun findByProductCertificateAndBalanceAt(
        @Param("certificate") certificate: String,
        @Param("balanceDate") balanceDate: LocalDate
    ): Balance?

}