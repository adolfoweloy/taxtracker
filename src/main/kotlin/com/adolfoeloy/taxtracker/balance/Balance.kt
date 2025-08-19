package com.adolfoeloy.taxtracker.balance

import com.adolfoeloy.taxtracker.product.Product
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "balance")
class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(name = "product_id")
    var productId: Int = 0

    @Column()
    var percent: Int = 0

    @Column()
    var principal: Int = 0

    @Column()
    var balance: Int = 0

    @Column()
    var interest: Int = 0

    @Column()
    var iof: Int = 0

    @Column(name = "br_tax")
    var brTax: Int = 0

    @Column(name = "balance_net")
    var balanceNet: Int = 0

    @Column(name = "balance_date")
    var balanceDate: LocalDate = LocalDate.now()

    @Column(name = "br_au_forex")
    var brAuForex: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    var product: Product? = null
}