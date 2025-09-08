package com.adolfoeloy.taxtracker.transaction

import com.adolfoeloy.taxtracker.product.Certificate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {

    /**
     * Finds all Transactions for a specific month and year.
     *
     * @param month the month to filter by (1-12).
     * @param year the year to filter by.
     * @return a list of Transactions for the specified month and year.
     */
    @Query("SELECT t FROM Transaction t WHERE EXTRACT(MONTH FROM t.paymentDate) = :month AND EXTRACT(YEAR FROM t.paymentDate) = :year")
    fun findByMonthAndYear(@Param("month") month: Int, @Param("year") year: Int): List<Transaction>

    /**
     * Finds a Transaction by its product certificate and payment date.
     *
     * @param certificate the certificate of the product.
     * @param paymentDate the payment date.
     * @return the Transaction if found, or null if not found.
     */
    @Query("SELECT t FROM Transaction t JOIN t.product p WHERE p.certificate = :certificate AND t.paymentDate = :paymentDate")
    fun findByProductCertificateAndPaymentDate(
        @Param("certificate") certificate: Certificate,
        @Param("paymentDate") paymentDate: LocalDate
    ): Transaction?
}
