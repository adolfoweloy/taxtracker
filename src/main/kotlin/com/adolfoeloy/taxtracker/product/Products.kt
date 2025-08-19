package com.adolfoeloy.taxtracker.product

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Products {

    companion object {
        fun createProduct(
            name: String,
            certificate: String,
            issuedAt: String,
            matureAt: String
        ): Product {

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val issuedAtDate = LocalDate.parse(issuedAt, formatter)
            val matureAtDate = LocalDate.parse(matureAt, formatter)

            return Product().apply {
                this.name = name
                this.certificate = certificate
                this.issuedAt = issuedAtDate
                this.matureAt = matureAtDate
            }
        }
    }


}