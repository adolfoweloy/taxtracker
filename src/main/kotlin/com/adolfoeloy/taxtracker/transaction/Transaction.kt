package com.adolfoeloy.taxtracker.transaction

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "transaction")
class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(name = "product_id")
    var productId: Int = 0

    @Column(name = "payment_date")
    var paymentDate: String = ""

    var percent: Int = 0

    var principal: Int = 0

    var redemption: Int = 0

    var interest: Int = 0

    var iof: Int = 0

    @Column(name = "br_tax")
    var brTax: Int = 0

    var credited: Int = 0

    var description: String = ""

    @Column(name = "br_au_forex")
    var brToAuForex: Int = 0
}