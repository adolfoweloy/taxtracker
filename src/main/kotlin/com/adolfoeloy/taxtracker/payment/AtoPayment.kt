package com.adolfoeloy.taxtracker.payment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "ato_payment")
class AtoPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var income: IncomeType = IncomeType.cdb

    @Column(name = "income_declared", nullable = false)
    var incomeDeclared: BigDecimal = BigDecimal.ZERO

    @Column(name = "tax_paid", nullable = false)
    var taxPaid: BigDecimal = BigDecimal.ZERO

    @Column(name = "payment_date", nullable = false)
    var paymentDate: LocalDate = LocalDate.now()
}
