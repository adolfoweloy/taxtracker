package com.adolfoeloy.taxtracker.vgbl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "vgbl_track")
class VGBLTrack {

    @Id
    var id: Int = 0

    @ManyToOne
    @JoinColumn(name = "cnpj")
    var vgblFund: VGBLFund? = null

    @Column(precision = 27, scale = 12)
    var quotas: BigDecimal = BigDecimal.ZERO

    @Column(name = "transaction_date")
    var transactionDate: LocalDate = LocalDate.now()

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    var transactionType: VGBLTransactionType = VGBLTransactionType.CONTRIBUTION

    @Column(name = "transaction_amount", precision = 27, scale = 12)
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(name = "quota_price", precision = 27, scale = 12)
    var quotaPrice: BigDecimal = BigDecimal.ZERO

    @Column(name = "br_tax", precision = 27, scale = 12)
    var brTax: BigDecimal = BigDecimal.ZERO
}

enum class VGBLTransactionType {
    CONTRIBUTION,
    REDEMPTION;

    override fun toString(): String {
        return this.name.lowercase()
    }
}