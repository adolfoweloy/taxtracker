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
            val normalizedCertificate = normalizeCertificate(certificate)

            return Product().apply {
                this.name = name
                this.certificate = normalizedCertificate
                this.issuedAt = issuedAtDate
                this.matureAt = matureAtDate
            }
        }

        private fun normalizeCertificate(certificate: String): String {
            return when (certificate.length) {
                13 -> {
                    val prefix = certificate.take(3) // "1260"
                    val suffix = certificate.substring(5) // "23458960"
                    prefix + "023700000" + suffix
                }
                20 -> certificate // Already in correct format
                else -> certificate // Return as-is for other lengths
            }
        }
    }


}