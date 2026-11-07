package com.adolfoeloy.taxtracker.vgbl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

/**
 * TODO: This entity could be called VGBL Transaction because it tracks my transactions on a given fund acquired or redeemed.
 */
@Entity
@Table(name = "vgbl_track")
class VGBLTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @ManyToOne
    @JoinColumn(name = "cnpj")
    var vgblFund: VGBLFund? = null

    /**
     * Total amount of quotas held
     */
    @Column(precision = 27, scale = 12)
    var quotas: BigDecimal = BigDecimal.ZERO

    @Column(name = "transaction_date")
    var transactionDate: LocalDate = LocalDate.now()

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    var transactionType: VGBLTransactionType = VGBLTransactionType.CONTRIBUTION

    /**
     * This represents the amount of money contributed or redeemed.
     */
    @Column(name = "amount", precision = 27, scale = 12)
    var amount: BigDecimal = BigDecimal.ZERO

    /**
     * Quota price according to CVM investment funds daily info website:
     * https://dados.cvm.gov.br/dados/FI/DOC/INF_DIARIO/DADOS/
     */
    @Column(name = "quota_price", precision = 27, scale = 12)
    var quotaPrice: BigDecimal = BigDecimal.ZERO

    /**
     * Total tax withhold in Brazil
     */
    @Column(name = "br_tax", precision = 27, scale = 12)
    var brTax: BigDecimal = BigDecimal.ZERO

    @Column(name = "full_redemption")
    var fullRedemption: Boolean = false
}

enum class VGBLTransactionType {
    CONTRIBUTION,
    REDEMPTION;

    override fun toString(): String {
        return this.name.lowercase()
    }
}