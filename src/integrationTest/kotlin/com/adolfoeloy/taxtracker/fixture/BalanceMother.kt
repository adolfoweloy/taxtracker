package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.report.Balance
import com.adolfoeloy.taxtracker.report.BalanceReport
import com.adolfoeloy.taxtracker.report.Product

class BalanceMother {

    companion object {

        fun withCDBFacil(
            issuedAt: String, maturityDate: String, principal: Int
        ): ProductBalance = ProductBalance(
            product = Product(
                name = "CDB Facil Bradesco",
                certificate = "123456",
                issuedAt = issuedAt,
                matureAt = maturityDate
            ),
            principal = principal
        )

        class ProductBalance(
            val product: Product,
            val principal: Int
        ) {
            fun withBalance(balanceAt: String, interest: Int, estimatedBrlTax: Int): Balance = Balance(
                balanceAt = balanceAt,
                entries = listOf(
                    BalanceReport(
                        product = product.name,
                        certificate = product.certificate,
                        issuedAt = product.issuedAt,
                        maturityDate = product.matureAt,
                        principal = principal,
                        interest = interest,
                        estimatedBrlTax = estimatedBrlTax
                    )
                )
            )
        }
    }


}