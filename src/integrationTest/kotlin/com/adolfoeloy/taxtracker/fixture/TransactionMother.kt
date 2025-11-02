package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.report.Product
import com.adolfoeloy.taxtracker.report.TransactionReport

class TransactionMother {

    companion object {

        fun withdrawFromCdbFacilAt(
            product: Product, paymentAt: String, amount: Int, interest: Int, tax: Int
        ): TransactionReport = TransactionReport(
            product = product.name,
            certificate = product.certificate,
            issuedAt = product.issuedAt,
            matureAt = product.matureAt,
            paymentAt = paymentAt,
            principal = 0,
            redemption = amount,
            interest = interest,
            brTax = tax,
            credit = 85,
            description = "Monthly interest payment",
            brToAuForex = 3.5.toBigDecimal()
        )

    }
}