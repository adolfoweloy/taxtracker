package com.adolfoeloy.taxtracker.balance

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

    /**
     * Finds all Balances for a specific month and year.
     *
     * @param month the month to filter by (1-12).
     * @param year the year to filter by.
     * @return a list of Balances for the specified month and year.
     */
    @Query("SELECT b FROM Balance b WHERE EXTRACT(MONTH FROM b.balanceDate) = :month AND EXTRACT(YEAR FROM b.balanceDate) = :year")
    fun findByMonthAndYear(@Param("month") month: Int, @Param("year") year: Int): List<Balance>

}